package dev.oop778.bindings.collection;

import dev.oop778.bindings.collection.implementation.map.KeyBindableMap;
import dev.oop778.bindings.collection.implementation.map.ValueBindableMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface BindableMap<B, K, V> {

    static <K, V> KeyBindableMap<K, V> wrapKeyBindable(Map<K, V> backing) {
        return new KeyBindableMap<>(backing);
    }

    static <K, V> ValueBindableMap<K, V> wrapValueBindable(Map<K, V> backing) {
        return new ValueBindableMap<>(backing);
    }

    int hashCode();

    boolean equals(Object o);

    int size();

    boolean isEmpty();

    boolean containsKey(K key);

    boolean containsValue(V value);

    V get(K key);

    CollectionInsertResult<B> put(K key, V value);

    V remove(K key);

    CollectionInsertResult<B> putAll(Map<? extends K, ? extends V> m);

    void clear();

    Set<K> keySet();

    Collection<V> values();

    Set<Map.Entry<K, V>> entrySet();

    V getOrDefault(Object key, V defaultValue);

    void forEach(BiConsumer<? super K, ? super V> action);

    void replaceAll(BiFunction<? super K, ? super V, ? extends V> function);

    CollectionInsertResult<B> putIfAbsent(K key, V value);
}
