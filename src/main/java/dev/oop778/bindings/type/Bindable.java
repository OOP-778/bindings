package dev.oop778.bindings.type;

import dev.oop778.bindings.Bindings;
import dev.oop778.bindings.enums.BindableFlag;
import dev.oop778.bindings.enums.BindingOrder;

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
        return Bindings.getInstance().getDirection(this, to) != -1;
    }

    default Bindable bindTo(Bindable bindable) {
        this.bindTo(bindable, BindingOrder.NORMAL);
        return this;
    }

    default Bindable bindTo(Bindable bindable, BindingOrder order) {
        Bindings.getInstance().bind(this, bindable, order);
        return this;
    }

    default Bindable unbindFrom(Bindable from) {
        Bindings.getInstance().unbind(this, from);
        return this;
    }

    default Bindable flag(BindableFlag ...flag) {
        Bindings.getInstance().flag(this, flag);
        return this;
    }

    default boolean isClosed() {
        return Bindings.getInstance().isClosed(this);
    }

    default String bindableDisplay() {
        final String simpleName = this.getClass().getSimpleName();
        if (!simpleName.isEmpty()) {
            return simpleName;
        }

        final String[] split = this.getClass().getTypeName().split("\\.");
        return split[split.length - 1];
    }

    interface NonBindable extends Bindable {
        static NonBindable create() {
            return new NonBindable() {};
        }

        @Override
        default void close() {
        }

        @Override
        default Bindable bindTo(Bindable to) {
            return this;
        }

        @Override
        default Bindable bindTo(Bindable to, BindingOrder order) {
            return this;
        }

        @Override
        default Bindable unbindFrom(Bindable from) {
            return this;
        }
    }
}
