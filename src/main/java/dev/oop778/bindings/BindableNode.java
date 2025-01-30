package dev.oop778.bindings;

import dev.oop778.bindings.enums.BindableFlag;
import dev.oop778.bindings.enums.BindingOrder;
import dev.oop778.bindings.type.Bindable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("FieldMayBeFinal")
@Getter
/*
 * NODE states:
 * (x of BindableFlag len) first bits
 * last one bit of for closed state
 * <p>
 * ENTRY params:
 * 0-31 - stored seq
 * 32-34 - order
 * 35 - direction
 */
public class BindableNode {
    private final Bindable bindable;
    private final AtomicReference<BindEntry[]> entries;
    private int states;
    private int childCounter;
    static int CLOSED_MASK = 1 << BindableFlag.values().length;
    static int ENTRY_DIRECTION_SHIFT = 35;
    static int ENTRY_ORDER_SHIFT = 32;
    static long ENTRY_SEQ_MASK = 0xFFFFFFFFL;
    static Comparator<BindEntry> ENTRY_COMPARATOR = Comparator
            // Compare by order
            .<BindEntry>comparingInt(entry -> unpackOrder(entry.params))

            // Compare by seq
            .thenComparingInt(entry -> unpackSeq(entry.params));
    private static final VarHandle STATE_HANDLE;
    private static final VarHandle CHILD_COUNTER_HANDLE;

    static {
        try {
            STATE_HANDLE = MethodHandles.lookup().findVarHandle(BindableNode.class, "states", int.class);
            CHILD_COUNTER_HANDLE = MethodHandles.lookup().findVarHandle(BindableNode.class, "childCounter", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public BindableNode(Bindable bindable) {
        this.bindable = bindable;
        this.entries = new AtomicReference<>(new BindEntry[0]);
        this.states = 0;
    }

    public static int unpackDirection(long pack) {
        return (int) ((pack >>> ENTRY_DIRECTION_SHIFT) & 0x1);
    }

    public static int unpackOrder(long pack) {
        return (int) ((pack >>> ENTRY_ORDER_SHIFT) & 0x7);
    }

    public static int unpackSeq(long pack) {
        return (int) ((pack & ENTRY_SEQ_MASK));
    }

    private static long packEntry(int direction, BindingOrder order, int seq) {
        final long directionLong = ((long) direction & 0x1) << ENTRY_DIRECTION_SHIFT;
        final long orderLong = ((long) order.ordinal() & 0x7) << ENTRY_ORDER_SHIFT;
        final long seqLong = (long) seq & 0xFFFFFFFFL;

        return directionLong | orderLong | seqLong;
    }

    public int nextSeq() {
        return (int) CHILD_COUNTER_HANDLE.getAndAdd(this, 1);
    }

    public boolean addConnection(BindableNode from, BindingOrder order, boolean isFrom) {
        BindEntry[] oldArray;
        BindEntry[] newArray;

        boolean result;

        do {
            oldArray = this.entries.get();

            // We've been closed down
            if (oldArray == null) {
                return false;
            }

            BindEntry bindEntry;
            if ((bindEntry = this.findEntry(from, oldArray)) != null) {
                newArray = oldArray;
                result = false;

                if (bindEntry.getDirection() == (isFrom ? 1 : 0)) {
                    throw new IllegalStateException("Circular binding detected");
                }

                continue;

            } else {
                bindEntry = new BindEntry(from, packEntry(isFrom ? 0 : 1, order, this.nextSeq()));
            }

            newArray = Arrays.copyOf(oldArray, oldArray.length + 1);

            int insertionPoint = Arrays.binarySearch(oldArray, bindEntry, ENTRY_COMPARATOR);
            if (insertionPoint < 0) {
                insertionPoint = -insertionPoint - 1;
            }

            System.arraycopy(oldArray, insertionPoint, newArray, insertionPoint + 1, oldArray.length - insertionPoint);

            newArray[insertionPoint] = bindEntry;
            result = true;
        } while (!this.entries.compareAndSet(oldArray, newArray));

        return result;
    }

    public boolean close() {
        if (!this.setState(CLOSED_MASK)) {
            return false;
        }

        BindEntry[] oldArray;
        do {
            oldArray = this.entries.get();

            for (final BindEntry bindEntry : oldArray) {
                bindEntry.node.handleClose(this, bindEntry.getDirection() == 0);
            }

        } while (!this.entries.compareAndSet(oldArray, null));

        return true;
    }

    public void handleClose(BindableNode node, boolean callClose) {
        final BindEntry entry = this.removeEntry(node);
        if (entry == null) {
            return;
        }

        if (callClose) {
            this.bindable.close();

        } else {
            if (this.hasFlag(BindableFlag.CLOSE_ON_UNBIND) || this.hasFlag(BindableFlag.CLOSE_WHEN_EMPTY) && this.entries.get().length == 0) {
                this.bindable.close();
            }
        }
    }

    public boolean isClosed() {
        return this.hasState(CLOSED_MASK);
    }

    public boolean hasFlag(BindableFlag flag) {
        return this.hasState(flag.getMask());
    }

    public boolean hasState(int flagBit) {
        final int current = (int) STATE_HANDLE.getVolatile(this);
        return (current & flagBit) != 0;
    }

    public void flag(BindableFlag... flag) {
        for (final BindableFlag bindableFlag : flag) {
            this.setState(bindableFlag.getMask());
        }
    }

    public boolean setState(int mask) {
        int prev;
        int updated;

        do {
            prev = (int) STATE_HANDLE.get(this);
            updated = prev | mask;

            if (prev == updated) {
                return false;
            }

        } while (!STATE_HANDLE.compareAndSet(this, prev, updated));

        return true;
    }

    public boolean trySetState(int mask) {
        final int state = (int) STATE_HANDLE.getVolatile(this);
        return STATE_HANDLE.compareAndSet(this, state, state | mask);
    }

    protected BindEntry findEntry(Bindable from) {
        final BindEntry[] snapshot = this.entries.get();
        if (snapshot == null) {
            return null;
        }

        for (final BindEntry bindEntry : snapshot) {
            if (bindEntry.node.getBindable() != from) {
                continue;
            }

            return bindEntry;
        }

        return null;
    }

    protected BindEntry findEntry(BindableNode from, BindEntry[] entries) {
        if (entries == null) {
            return null;
        }

        for (final BindEntry entry : entries) {
            if (entry.node != from) {
                continue;
            }

            return entry;
        }

        return null;
    }

    protected BindEntry removeEntry(BindableNode what) {
        BindEntry[] oldEntries;
        BindEntry[] newEntries;
        BindEntry removed;

        do {
            oldEntries = this.entries.get();
            if (oldEntries == null) {
                return null;
            }

            int index = -1;
            for (int i = 0; i < oldEntries.length; i++) {
                if (oldEntries[i].node == what) {
                    index = i;
                    break;
                }
            }

            if (index == -1) {
                return null;
            }

            removed = oldEntries[index];
            newEntries = Arrays.copyOf(oldEntries, oldEntries.length - 1);

            System.arraycopy(oldEntries, 0, newEntries, 0, index);

            final int tailCount = oldEntries.length - (index + 1);
            if (tailCount > 0) {
                System.arraycopy(oldEntries, index + 1, newEntries, index, tailCount);
            }

        } while (!this.entries.compareAndSet(oldEntries, newEntries));

        return removed;
    }

    @Getter
    @RequiredArgsConstructor
    protected static class BindEntry {
        private final BindableNode node;
        private final long params;

        public BindingOrder getOrder() {
            return BindingOrder.values()[unpackOrder(this.params)];
        }

        public int getDirection() {
            return unpackDirection(this.params);
        }

        public int getSequence() {
            return unpackSeq(this.params);
        }
    }
}
