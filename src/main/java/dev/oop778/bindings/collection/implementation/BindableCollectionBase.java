package dev.oop778.bindings.collection.implementation;

import dev.oop778.bindings.collection.CollectionReference;
import dev.oop778.bindings.type.Bindable;
import dev.oop778.bindings.util.Entry;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.Getter;

@Getter
public abstract class BindableCollectionBase<T> {
    protected final Map<CollectionReference<T>, CollectionReferenceBindable> valueToBindable;

    protected BindableCollectionBase() {
        this.valueToBindable = new ConcurrentHashMap<>();
    }

    protected Entry<T, Bindable> internalPostInsert(T value) {
        final CollectionReference<T> wrap = this.wrap(value);

        final CollectionReferenceBindable newBindable = new CollectionReferenceBindable() {
            @Override
            public void close() {
                CollectionReferenceBindable.super.close();
                BindableCollectionBase.this.internalHandleRemove(value);
            }
        };
        final CollectionReferenceBindable currentValue = this.valueToBindable.put(wrap, newBindable);

        if (currentValue != null) {
            currentValue.close();
        }

        return Entry.create(value, newBindable);
    }

    protected void internalHandleRemove(T value) {
        this.internalPostRemove(value);
    }

    protected void internalPostRemove(T value) {
        final CollectionReferenceBindable bindable = this.valueToBindable.remove(this.wrap(value));
        if (bindable != null) {
            bindable.internalClose();
        }
    }

    protected abstract CollectionReference<T> wrap(T value);

    interface CollectionReferenceBindable extends Bindable {
        default void internalClose() {
            Bindable.super.close();
        }
    }

    public class BackingIterator<V> implements Iterator<V> {
        private final Iterator<V> backingIterator;
        private final Function<V, T> valueExtractor;
        private V current;

        public BackingIterator(Iterator<V> backingIterator, Function<V, T> valueExtractor) {
            this.backingIterator = backingIterator;
            this.valueExtractor = valueExtractor;
        }

        @Override
        public boolean hasNext() {
            return this.backingIterator.hasNext();
        }

        @Override
        public V next() {
            this.current = this.backingIterator.next();
            return this.current;
        }

        @Override
        public void remove() {
            this.backingIterator.remove();
            if (this.current != null) {
                BindableCollectionBase.this.internalPostRemove(this.valueExtractor.apply(this.current));
            }
        }
    }
}
