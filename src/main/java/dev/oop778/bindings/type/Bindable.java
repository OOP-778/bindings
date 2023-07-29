package dev.oop778.bindings.type;

import dev.oop778.bindings.BindingOrder;
import dev.oop778.bindings.Bindings;

public interface Bindable {

    static Bindable create() {
        return new Bindable() {
            @Override
            public void close() {
                Bindable.super.close();
            }
        };
    }

    default void close() {
        Bindings.getInstance().close(this);
    }

    static Bindable create(Runnable runnable) {
        return new Bindable() {
            @Override
            public void close() {
                Bindable.super.close();
                runnable.run();
            }
        };
    }

    default boolean isBinded(Bindable to) {
        return Bindings.getInstance().getBindedDirection(this, to) != null;
    }

    default Bindable bindTo(Bindable bindable) {
        this.bindTo(bindable, BindingOrder.NORMAL);
        return this;
    }

    default Bindable bindTo(Bindable bindable, BindingOrder order) {
        Bindings.getInstance().bind(this, bindable, order);
        return this;
    }

    enum Flag {}
}
