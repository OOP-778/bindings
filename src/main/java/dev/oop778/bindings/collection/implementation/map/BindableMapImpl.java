package dev.oop778.bindings.collection.implementation.map;

import dev.oop778.bindings.collection.BindableMap;
import dev.oop778.bindings.collection.CollectionReference;
import dev.oop778.bindings.collection.CollectionInsertResult;
import dev.oop778.bindings.collection.implementation.BindableCollectionBase;
import dev.oop778.bindings.type.Bindable;
import dev.oop778.bindings.util.Entry;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class BindableMapImpl<B, K, V> extends BindableCollectionBase<Object> implements BindableMap<B, K, V> {
    private final Map<K, V> backing;
    private final Set<K> keys;
    private final Collection<V> values;
    private final Set<Map.Entry<K, V>> entrySet;
    private final boolean keyBindable;

    public BindableMapImpl(Map<K, V> backing, boolean keyBindable) {
        this.backing = backing;
        this.keys = new ForwardingKeyCollection();
        this.values = new ForwardingValueCollection();
        this.entrySet = new ForwardingEntrySet();
        this.keyBindable = keyBindable;
    }

    @Override
    public int size() {
        return this.backing.size();
    }

    @Override
    public boolean isEmpty() {
        return this.backing.isEmpty();
    }

    @Override
    public boolean containsKey(K key) {
        return this.backing.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return this.backing.containsValue(value);
    }

    @Override
    public V get(K key) {
        return this.backing.get(key);
    }

    @Override
    public CollectionInsertResult<B> put(K key, V value) {
        final V removed = this.backing.put(key, value);
        final Entry<Object, Bindable> result = this.internalPostInsert(this.keyBindable ? key : value);

        if (removed != null) {
            this.internalPostRemove(removed);
        }

        return (CollectionInsertResult<B>) CollectionInsertResult.success(result);
    }

    @Override
    public V remove(K key) {
        final V removed = this.backing.remove(key);
        if (removed != null) {
            this.internalPostRemove(removed);
        }

        return removed;
    }

    @Override
    public CollectionInsertResult<B> putAll(Map<? extends K, ? extends V> m) {
        final List<Map.Entry<B, Bindable>> added = new ArrayList<>();
        for (final Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.backing.put(entry.getKey(), entry.getValue());
            added.add((Map.Entry<B, Bindable>) this.internalPostInsert(entry.getValue()));
        }

        return CollectionInsertResult.success(added.toArray(new Map.Entry[0]));
    }

    @Override
    public void clear() {
        final Iterator<V> iterator = this.values().iterator();
        while (iterator.hasNext()) {
            this.internalPostRemove(iterator.next());
            iterator.remove();
        }
    }

    @Override
    public Set<K> keySet() {
        return this.keys;
    }

    @Override
    public Collection<V> values() {
        return this.values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return this.entrySet;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return Optional.ofNullable(this.backing.get(key)).orElse(defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        this.backing.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        this.backing.replaceAll((key, value) -> {
            final V newValue = function.apply(key, value);
            if (newValue == null) {
                this.internalPostRemove(value);
                return null;
            }

            if (this.wrap(newValue).equals(this.wrap(value))) {
                return value;
            }

            this.internalPostRemove(value);
            this.internalPostInsert(newValue);

            return newValue;
        });
    }

    @Override
    public CollectionInsertResult<B> putIfAbsent(K key, V value) {
        final V previous = this.backing.putIfAbsent(key, value);
        if (previous == null) {
            return (CollectionInsertResult<B>) CollectionInsertResult.success(this.internalPostInsert(this.keyBindable ? key : value));
        }

        return (CollectionInsertResult<B>) CollectionInsertResult.EMPTY;
    }

    @Override
    protected void internalHandleRemove(Object object) {
        super.internalHandleRemove(object);
        if (this.keyBindable) {
            this.backing.remove(object);
        } else {
            this.backing.values().remove(object);
        }
    }

    @Override
    protected CollectionReference<Object> wrap(Object value) {
        return this.backing.getClass().getSimpleName().contains("Hash") ? CollectionReference.hashCode(value) : CollectionReference.identity(value);
    }

    protected K findKeyForValue(V value) {
        for (final Map.Entry<K, V> entry : this.backing.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public class ForwardingValueCollection extends AbstractCollection<V> {

        @Override
        public Iterator<V> iterator() {
            return new BackingIterator<V>(
                BindableMapImpl.this.backing.values().iterator(),
                (value) -> BindableMapImpl.this.keyBindable
                           ? BindableMapImpl.this.findKeyForValue(value)
                           : value
            );
        }

        @Override
        public int size() {
            return BindableMapImpl.this.backing.size();
        }
    }

    public class ForwardingKeyCollection extends AbstractCollection<K> implements Set<K> {

        @Override
        public Iterator<K> iterator() {
            return new BackingIterator<K>(
                BindableMapImpl.this.backing.keySet().iterator(), (key) -> BindableMapImpl.this.keyBindable ? key : BindableMapImpl.this.backing.get(key));
        }

        @Override
        public int size() {
            return BindableMapImpl.this.backing.size();
        }
    }

    public class ForwardingEntrySet extends AbstractCollection<Map.Entry<K, V>> implements Set<Map.Entry<K, V>> {

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new BackingIterator<Map.Entry<K, V>>(BindableMapImpl.this.backing.entrySet().iterator(), (entry) -> BindableMapImpl.this.keyBindable ?
                                                                                                                       entry.getKey() : entry.getValue());
        }

        @Override
        public int size() {
            return BindableMapImpl.this.backing.size();
        }
    }
}
