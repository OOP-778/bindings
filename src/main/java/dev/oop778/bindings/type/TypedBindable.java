package dev.oop778.bindings.type;

import dev.oop778.bindings.BindingOrder;

public interface TypedBindable<T extends Bindable> extends Bindable {

    @Override
    default T bindTo(Bindable bindable) {
        return (T) Bindable.super.bindTo(bindable);
    }

    @Override
    default T bindTo(Bindable bindable, BindingOrder order) {
        return (T) Bindable.super.bindTo(bindable, order);
    }
}
