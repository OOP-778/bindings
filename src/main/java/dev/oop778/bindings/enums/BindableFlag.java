package dev.oop778.bindings.enums;

public enum BindableFlag {
    // Will close bindable once all children unbind from it
    CLOSE_WHEN_EMPTY,

    // Close when first children unbinds
    CLOSE_ON_UNBIND,
}
