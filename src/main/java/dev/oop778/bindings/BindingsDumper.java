package dev.oop778.bindings;

import dev.oop778.bindings.enums.BindableFlag;
import dev.oop778.bindings.enums.BindingOrder;
import dev.oop778.bindings.listener.BindingsListener;
import dev.oop778.bindings.type.Bindable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;

public class BindingsDumper {
    private static void collectParams(BindableNode node, Map<String, Object> params) {
        final List<BindableFlag> flags = new ArrayList<>();
        for (final BindableFlag flag : BindableFlag.values()) {
            if (node.hasFlag(flag)) {
                flags.add(flag);
            }
        }

        if (!flags.isEmpty()) {
            params.put("Flags", flags);
        }

        for (final BindingsListener listener : Bindings.getInstance().getListeners()) {
            listener.onDump(node, params);
        }
    }

    private static Map<String, EdgeDump> collectEdges(BindableNode node, Map<Class<?>, TypeCounter> typeCounter) {
        final Map<String, EdgeDump> edges = new LinkedHashMap<>();
        final BindableNode.BindEntry[] entriesSnapshot = node.getEntries().get();
        if (entriesSnapshot == null) {
            return edges;
        }

        for (final BindableNode.BindEntry bindEntry : entriesSnapshot) {
            final int identifier = getIdentifier(bindEntry.getNode(), typeCounter);
            final String display = bindEntry.getNode().getBindable().bindableDisplay() + "{" + identifier + "}";
            final String direction = bindEntry.getDirection() == 0 ? "FROM" : "TO";

            edges.put(display, new EdgeDump(display, direction, bindEntry.getOrder(), bindEntry.getSequence()));
        }

        return edges;
    }

    private static int getIdentifier(BindableNode node, Map<Class<?>, TypeCounter> typeCounter) {
        return typeCounter
                .computeIfAbsent(node.getBindable().getClass(), ($) -> new TypeCounter())
                .getIdentifier(node.getBindable());
    }

    public static List<NodeDump> dump() {
        final Map<String, NodeDump> nodes = new LinkedHashMap<>();
        final Map<Class<?>, TypeCounter> typeCounter = new IdentityHashMap<>();

        for (final BindableNode node : Bindings.getInstance().getBindables().values()) {
            final int identifier = getIdentifier(node, typeCounter);

            final String display = node.getBindable().bindableDisplay() + "{" + identifier + "}";
            final boolean closed = node.isClosed();

            final Map<String, Object> params = new LinkedHashMap<>();
            collectParams(node, params);

            final Map<String, EdgeDump> edgeDumps = collectEdges(node, typeCounter);
            nodes.put(display, new NodeDump(
                    node.getBindable().getClass(),
                    display,
                    closed,
                    params,
                    edgeDumps
            ));
        }

        final Map<NodeDump, List<NodeDump>> adjacencyMap = new IdentityHashMap<>();
        for (final NodeDump node : nodes.values()) {
            adjacencyMap.putIfAbsent(node, new ArrayList<>());
            for (final String edgeName : node.getEdges().keySet()) {
                final NodeDump target = nodes.get(edgeName);
                if (target != null) {
                    adjacencyMap.get(node).add(target);
                    adjacencyMap.putIfAbsent(target, new ArrayList<>());
                    adjacencyMap.get(target).add(node);
                }
            }
        }

        // Step 3: Cluster nodes using DFS or BFS
        final List<List<NodeDump>> clusters = new ArrayList<>();
        final Set<NodeDump> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        for (final NodeDump node : nodes.values()) {
            if (!visited.contains(node)) {
                final List<NodeDump> cluster = new ArrayList<>();
                exploreCluster(node, adjacencyMap, visited, cluster);
                clusters.add(cluster);
            }
        }

        // Step 4: Sort clusters and individual clusters internally
        clusters.sort(Comparator.<List<NodeDump>>comparingInt(List::size).reversed()); // Larger clusters first
        for (final List<NodeDump> cluster : clusters) {
            cluster.sort(Comparator.comparing(NodeDump::getDisplay));
        }

        // Step 5: Flatten and return sorted nodes
        final List<NodeDump> sortedNodes = new ArrayList<>();
        for (final List<NodeDump> cluster : clusters) {
            sortedNodes.addAll(cluster);
        }

        return sortedNodes;
    }

    // Helper: DFS traversal for clustering
    private static void exploreCluster(NodeDump node, Map<NodeDump, List<NodeDump>> adjacencyMap,
                                       Set<NodeDump> visited, List<NodeDump> cluster) {
        final Stack<NodeDump> stack = new Stack<>();
        stack.push(node);
        while (!stack.isEmpty()) {
            final NodeDump current = stack.pop();
            if (visited.add(current)) {
                cluster.add(current);
                stack.addAll(adjacencyMap.getOrDefault(current, Collections.emptyList()));
            }
        }
    }

    @SneakyThrows
    public static void dumpToFile(Path file) {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()))) {
            final List<NodeDump> dump = dump();
            final Map<Class<?>, Integer> typeCounts = new IdentityHashMap<>();
            for (final NodeDump nodeDump : dump) {
                typeCounts.merge(nodeDump.getType(), 1, Integer::sum);
            }

            writer.write("=== Bindings Dump ===\n");
            writer.write("Timestamp: " + new Date() + "\n\n");

            writer.write("Counts By Class:\n");
            for (final Map.Entry<Class<?>, Integer> classCountEntry : typeCounts.entrySet()) {
                writer.write("\t" + classCountEntry.getKey().getName() + ": " + classCountEntry.getValue() + "\n");
            }

            writer.append("\n");

            for (final NodeDump nodeDump : dump) {
                nodeDump.printTo(writer);
                writer.append("\n");
            }
        }
    }

    public static class TypeCounter {
        private final Map<Bindable, Integer> typeCounter = new IdentityHashMap<>();
        private int counter;

        public int getIdentifier(Bindable bindable) {
            return this.typeCounter.computeIfAbsent(bindable, ($) -> this.counter++);
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class NodeDump {
        private final Class<?> type;
        private final String display;
        private final boolean closed;
        private final Map<String, Object> params;
        private final Map<String, EdgeDump> edges;

        @SneakyThrows
        public void printTo(Writer out) {
            out.append("Node: ").append(this.display)
                    .append(this.closed ? " [CLOSED]" : "")
                    .append("\n");

            if (!this.params.isEmpty()) {
                for (final Map.Entry<String, Object> keyValue : this.params.entrySet()) {
                    out.append("%s = %s".formatted(keyValue.getKey(), keyValue.getValue()));
                    out.append("\n");
                }
            }

            if (!this.edges.isEmpty()) {
                out.append("  Edges:\n");
                for (final EdgeDump edge : this.edges.values()) {
                    out.append("    -> ")
                            .append(edge.targetName)
                            .append(" [direction=")
                            .append(String.valueOf(edge.direction))
                            .append(", order=")
                            .append(String.valueOf(edge.order))
                            .append(", seq=")
                            .append(String.valueOf(edge.seq))
                            .append("]\n");
                }
            } else {
                out.append("  (No edges)\n");
            }
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class EdgeDump {
        private final String targetName;
        private final String direction;
        private final BindingOrder order;
        private final int seq;
    }
}
