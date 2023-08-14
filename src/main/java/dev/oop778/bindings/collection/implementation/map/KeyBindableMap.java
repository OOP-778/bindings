package dev.oop778.bindings.collection.implementation.map;

import java.util.Map;

public class KeyBindableMap<K, V> extends BindableMapImpl<K, K, V> {

    public KeyBindableMap(Map<K, V> backing) {
        super(backing, true);
    }
}
