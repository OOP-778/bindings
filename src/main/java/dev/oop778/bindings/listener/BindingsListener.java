package dev.oop778.bindings.listener;

import dev.oop778.bindings.BindableNode;
import dev.oop778.bindings.type.Bindable;

import java.util.Map;

public interface BindingsListener {
    default void onNodeCreate(Bindable bindable, BindableNode node) {}

    default void onBindableClose(Bindable bindable, BindableNode node) {}

    default void onDump(BindableNode node, Map<String, Object> params) {}
}
