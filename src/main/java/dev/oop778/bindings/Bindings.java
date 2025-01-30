package dev.oop778.bindings;

import dev.oop778.bindings.enums.BindableFlag;
import dev.oop778.bindings.enums.BindingOrder;
import dev.oop778.bindings.listener.BindingsListener;
import dev.oop778.bindings.type.Bindable;
import dev.oop778.bindings.type.BindableOnce;
import dev.oop778.bindings.util.map.IdentityMap;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

@Getter(AccessLevel.PROTECTED)
public class Bindings {
    private final IdentityMap<Bindable, BindableNode> bindables = new IdentityMap<>();
    private final List<BindingsListener> listeners = new CopyOnWriteArrayList<>();
    private static final Bindings INSTANCE = new Bindings();

    public static Bindings getInstance() {
        return INSTANCE;
    }

    public void addListener(BindingsListener listener) {
        this.listeners.add(listener);
    }

    public boolean bind(Bindable what, Bindable to, BindingOrder order) {
        if (what == to) {
            throw new IllegalArgumentException("Cannot bind a bindable to itself");
        }

        final BindableNode toNode = to instanceof BindableOnce ? this.findNode(to).orElse(null) : this.getOrCreateNode(to);

        if (toNode != null) {
            final BindableNode whatNode = what instanceof BindableOnce ? this.findNode(what).orElse(null) : this.getOrCreateNode(what);
            if (whatNode != null && toNode.addConnection(whatNode,order,true)) {
                return whatNode.addConnection(toNode, order, false);
            }
        }

        return false;
    }

    public void alive(Bindable bindable) {
        this.getOrCreateNode(bindable);
    }

    public void flag(Bindable bindable, BindableFlag[] flag) {
        this.getOrCreateNode(bindable).flag(flag);
    }

    public boolean isClosed(Bindable bindable) {
        return this.bindables.get(bindable) == null;
    }

    public void unbind(Bindable what, Bindable from) {
        this.findNode(what).ifPresent((whatNode) -> this.findNode(from).ifPresent((fromNode) -> {
            whatNode.handleClose(fromNode, false);
            fromNode.handleClose(whatNode, false);
        }));
    }

    public boolean close(Bindable bindable) {
        final BindableNode node = this.bindables.remove(bindable);
        if (node == null) {
            return false;
        }

        final boolean closed = node.close();
        if (closed) {
            for (final BindingsListener listener : this.listeners) {
                listener.onBindableClose(bindable, node);
            }
        }

        return closed;
    }

    public int getDirection(Bindable what, Bindable from) {
        return this.findNode(what)
                .map(node -> node.findEntry(from))
                .map(BindableNode.BindEntry::getDirection).orElse(-1);
    }

    public int size() {
        return this.bindables.size();
    }

    public Collection<Bindable> collectBindables(Predicate<Bindable> collectPredicate) {
        final Set<Bindable> bindables = Collections.newSetFromMap(new IdentityHashMap<>());
        for (final BindableNode bindableNode : this.bindables.values()) {
            final Bindable bindable = bindableNode.getBindable();
            if (collectPredicate.test(bindable)) {
                bindables.add(bindable);
            }
        }

        return bindables;
    }

    public Collection<Bindable> collectBindablesOf(String packageName) {
        return this.collectBindables((bindable) -> bindable.getClass().getPackage().getName().startsWith(packageName));
    }

    protected Optional<BindableNode> findNode(Bindable bindable) {
        return Optional.ofNullable(this.bindables.get(bindable));
    }

    private BindableNode getOrCreateNode(Bindable bindable) {
        return this.bindables.getIfAbsent(
                bindable,
                () -> {
                    final BindableNode bindableNode = new BindableNode(bindable);
                    for (final BindingsListener listener : this.listeners) {
                        listener.onNodeCreate(bindable, bindableNode);
                    }

                    return bindableNode;
                }
        );
    }
}
