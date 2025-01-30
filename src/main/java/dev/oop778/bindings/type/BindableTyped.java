package dev.oop778.bindings.type;

import dev.oop778.bindings.enums.BindingOrder;

public interface BindableTyped<T extends Bindable> extends Bindable {

    @Override
    default T bindTo(Bindable bindable) {
        return (T) Bindable.super.bindTo(bindable);
    }

    @Override
    default T bindTo(Bindable bindable, BindingOrder order) {
        return (T) Bindable.super.bindTo(bindable, order);
    }

    @Override
    default T unbindFrom(Bindable from) {
        return (T) Bindable.super.unbindFrom(from);
    }

    interface NonBindable<T extends Bindable> extends BindableTyped<T> {
        @Override
        default void close() {}

        @Override
        default T bindTo(Bindable to, BindingOrder order) {
            return (T) this;
        }

        @Override
        default T bindTo(Bindable to) {
            return (T) this;
        }

        @Override
        default T unbindFrom(Bindable from) {
            return BindableTyped.super.unbindFrom(from);
        }

        static BindableTyped<? extends Bindable> create() {
            return new BindableTyped.NonBindable<Bindable>() {};
        }
    }

    interface Once<T extends BindableOnce> extends BindableOnce {
        @Override
        default T alive() {
            return (T) BindableOnce.super.alive();
        }
    }
}
