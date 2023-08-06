package dev.oop778.bindings.type;

import dev.oop778.bindings.enums.BindingOrder;

public interface TypedBindable<T extends Bindable> extends Bindable {

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

    interface NonBindable<T extends Bindable> extends TypedBindable<T> {
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
            return TypedBindable.super.unbindFrom(from);
        }
        static TypedBindable<? extends Bindable> create() {
            return new TypedBindable.NonBindable<Bindable>() {};
        }
    }
}
