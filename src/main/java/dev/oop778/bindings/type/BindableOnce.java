package dev.oop778.bindings.type;

import dev.oop778.bindings.Bindings;

public interface BindableOnce extends Bindable {

    default BindableOnce alive() {
        Bindings.getInstance().alive(this);
        return this;
    }

    public static BindableOnce create(Runnable runnable) {
        return new BindableOnce() {
            @Override
            public void close() {
                BindableOnce.super.close();
                runnable.run();
            }
        };
    }
}
