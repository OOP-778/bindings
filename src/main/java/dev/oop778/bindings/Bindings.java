package dev.oop778.bindings;

import dev.oop778.bindings.enums.BindableFlag;
import dev.oop778.bindings.enums.BindingOrder;
import dev.oop778.bindings.type.Bindable;
import dev.oop778.bindings.util.Entry;
import dev.oop778.bindings.util.JsonUtility;
import dev.oop778.bindings.util.ObjectTypeUtility;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;

public class Bindings {
    private final Map<Integer, BindableNode> bindableNodesByHash = new ConcurrentHashMap<>();
    private static final Bindings INSTANCE = new Bindings();

    public static Bindings getInstance() {
        return INSTANCE;
    }

    public void bind(Bindable what, Bindable to, BindingOrder order) {
        if (what == to) {
            throw new IllegalArgumentException("Cannot bind a bindable to itself");
        }

        final BindableNode whatNode = this.getOrCreateNode(what);
        final BindableNode toNode = this.getOrCreateNode(to);

        if (whatNode.addConnection(toNode, order, BindableNode.Direction.TO)) {
            toNode.addConnection(whatNode, order, BindableNode.Direction.FROM);
        }
    }

    private BindableNode getOrCreateNode(Bindable bindable) {
        return this.bindableNodesByHash.computeIfAbsent(
            System.identityHashCode(bindable),
            ($) -> new BindableNode(bindable)
        );
    }

    public void flag(Bindable bindable, BindableFlag[] flag) {
        this.getOrCreateNode(bindable).flag(flag);
    }

    public boolean isClosed(Bindable bindable) {
        return !this.findNode(bindable).isPresent();
    }

    private Optional<BindableNode> findNode(Bindable bindable) {
        return Optional.ofNullable(this.bindableNodesByHash.get(System.identityHashCode(bindable)));
    }

    public void unbind(Bindable what, Bindable from) {
        this.findNode(what).ifPresent((whatNode) -> this.findNode(from).ifPresent((fromNode) -> {
            whatNode.handleClose(fromNode, false);
            fromNode.handleClose(whatNode, false);
        }));
    }

    public boolean close(Bindable bindable) {
        final BindableNode node = this.bindableNodesByHash.remove(System.identityHashCode(bindable));
        if (node == null) {
            return false;
        }

        // close the node
        node.close();

        return true;
    }

    public BindableNode.Direction getBindedDirection(Bindable what, Bindable from) {
        final BindableNode whatNode = this.bindableNodesByHash.get(System.identityHashCode(what));
        if (whatNode == null) {
            return null;
        }

        final BindableNode.BindEntry bindEntry = whatNode.getBindEntriesByHash().get(System.identityHashCode(from));
        if (bindEntry == null) {
            return null;
        }

        return bindEntry.getDirection();
    }

    public int size() {
        return this.bindableNodesByHash.size();
    }

    public boolean contains(Bindable parent) {
        return this.bindableNodesByHash.containsKey(System.identityHashCode(parent));
    }

    public Collection<Bindable> collectBindables(Predicate<Bindable> collectPredicate) {
        final Set<Bindable> bindables = Collections.newSetFromMap(new IdentityHashMap<>());
        for (final BindableNode bindableNode : this.bindableNodesByHash.values()) {
            final Bindable bindable = bindableNode.getBindable();
            if (collectPredicate.test(bindable)) {
                bindables.add(bindable);
            }
        }

        return bindables;
    }

    public Collection<Bindable> collectBindablesOf(String packageName) {
        return collectBindables((bindable) -> bindable.getClass().getPackage().getName().startsWith(packageName));
    }

    @SneakyThrows
    public void dumpToFile(File file) {
        if (!file.getName().endsWith("html")) {
            throw new IllegalArgumentException("File name must end with .html");
        }

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        // Collect data
        final Collection<DumpEntry> dump = this.dump();

        // Read template
        try (final InputStream resourceAsStream = Bindings.class.getResourceAsStream("/dump_template.html")) {
            if (resourceAsStream == null) {
                throw new IllegalStateException("Template not found");
            }

            // Write template
            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("%DATA_ENCODED_JSON%")) {
                        line = line.replace("%DATA_ENCODED_JSON%", Base64.getEncoder().encodeToString(this.toJson(dump).getBytes(StandardCharsets.UTF_8)));
                    } else if (line.contains("%CREATED_INSTANT_STRING%")) {
                        line = line.replace("%CREATED_INSTANT_STRING%", String.valueOf(Instant.now().toEpochMilli()));
                    }

                    writer.write(line);
                    writer.write("\n");
                }
            }
        }
    }

    public Collection<DumpEntry> dump() {
        final AtomicInteger idCounter = new AtomicInteger(0);
        final Map<Integer, DumpEntry> byId = new HashMap<>();

        for (final BindableNode node : this.bindableNodesByHash.values()) {
            final DumpEntry dumpEntry = byId.computeIfAbsent(
                System.identityHashCode(node.getBindable()),
                ($) -> new DumpEntry(
                    idCounter.incrementAndGet(),
                    String.format("%s (%s)", ObjectTypeUtility.get(node.getBindable()), System.identityHashCode(node.getBindable())),
                    new ArrayList<>(),
                    node.getCreateStack(),
                    node.getCreatedAtMs()
                )
            );

            for (final BindableNode.BindEntry bindEntry : node.getBindEntriesByHash().values()) {
                if (bindEntry.getDirection() != BindableNode.Direction.FROM) {
                    continue;
                }

                dumpEntry.bindedTo.add(byId.computeIfAbsent(
                        System.identityHashCode(bindEntry.getNode().getBindable()),
                        ($) -> new DumpEntry(
                            idCounter.incrementAndGet(),
                            String.format("%s (%s)", ObjectTypeUtility.get(bindEntry.getNode().getBindable()), System.identityHashCode(bindEntry.getNode())),
                            new ArrayList<>(),
                            bindEntry.getNode().getCreateStack(),
                            bindEntry.getNode().getCreatedAtMs()
                        )
                    ).id
                );
            }
        }

        return byId.values()
            .stream()
            .sorted(Comparator.comparingInt(a -> a.id))
            .collect(Collectors.toList());
    }

    private String toJson(Collection<DumpEntry> collection) {
        return JsonUtility.writeCollection(collection, DumpEntry::toJson);
    }

    @AllArgsConstructor
    @ToString
    public static class DumpEntry implements JsonUtility.JsonSerializable {
        private final int id;
        private final String name;
        private final List<Integer> bindedTo;
        private final List<String> stack;
        private final long createdMs;

        @Override
        public String toJson() {
            return JsonUtility.write(
                Entry.create("id", this.id),
                Entry.create("name", this.name),
                Entry.create("binded_to", this.bindedTo),
                Entry.create("stack", this.stack == null ? Collections.singletonList("No Stack") : this.stack),
                Entry.create("live_time_ms", System.currentTimeMillis() - this.createdMs)
            );
        }
    }
}
