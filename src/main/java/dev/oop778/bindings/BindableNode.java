package dev.oop778.bindings;

import dev.oop778.bindings.enums.BindingOrder;
import dev.oop778.bindings.stack.StackCollector;
import dev.oop778.bindings.type.Bindable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class BindableNode {
    private final Bindable bindable;
    private final ConcurrentSkipListSet<BindEntry> bindEntries;
    private final Map<Integer, BindEntry> bindEntriesByHash;
    private final AtomicBoolean closed;
    private final List<String> createStack;

    public BindableNode(Bindable bindable) {
        this.bindable = bindable;
        this.bindEntries = new ConcurrentSkipListSet<>(Comparator
            // First compare by direction
            .<BindEntry>comparingInt((entry) -> entry.direction.ordinal())
            // Then compare by binding order
            .thenComparingInt((entry) -> entry.order.ordinal())
            // Then finally to keep the right insert order we compare by creation time
            .thenComparingLong((entry) -> entry.createdAtNs));
        this.bindEntriesByHash = new ConcurrentHashMap<>();
        this.closed = new AtomicBoolean(false);
        this.createStack = BindingsOptions.ENABLE_TRACING ? StackCollector.collectStack() : null;
    }

    public boolean addConnection(BindableNode node, BindingOrder order, Direction direction) {
        final BindEntry bindEntry = this.bindEntriesByHash.get(System.identityHashCode(node.bindable));
        if (bindEntry != null) {
            if (bindEntry.getDirection() != direction) {
                throw new IllegalStateException(String.format("Circular binding detected between %s and %s", this.bindable, node.bindable));
            }

            return false;
        }

        final BindEntry entry = new BindEntry(order, direction, node);
        this.bindEntriesByHash.put(System.identityHashCode(entry.node.bindable), entry);
        this.bindEntries.add(entry);
        return true;
    }

    public void close() {
        for (final BindEntry bindEntry : this.bindEntries) {
            bindEntry.node.handleClose(this, bindEntry.direction == Direction.FROM);
        }
    }

    public void handleClose(BindableNode node, boolean callClose) {
        final BindEntry remove = this.bindEntriesByHash.remove(System.identityHashCode(node.bindable));
        if (remove != null) {
            this.bindEntries.remove(remove);

            if (callClose) {
                this.bindable.close();
            }
        }
    }

    public enum Direction {
        TO,
        FROM
    }

    @Getter
    @RequiredArgsConstructor
    protected static class BindEntry {
        private final BindingOrder order;
        private final Direction direction;
        private final BindableNode node;
        private final long createdAtNs = System.nanoTime();
    }
}
