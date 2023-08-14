package dev.oop778.bindings.collection.implementation.map;

import java.util.Map;

public class ValueBindableMap<K, V> extends BindableMapImpl<V, K, V> {

    public ValueBindableMap(Map<K, V> backing) {
        super(backing, false);
    }
}
