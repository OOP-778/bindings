package dev.oop778.bindings.collection.implementation;

import dev.oop778.bindings.collection.BindableCollection;
import dev.oop778.bindings.collection.CollectionReference;
import dev.oop778.bindings.collection.CollectionInsertResult;
import dev.oop778.bindings.type.Bindable;
import dev.oop778.bindings.util.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.NonNull;

public class BindableCollectionImpl<T> extends BindableCollectionBase<T> implements BindableCollection<T> {
    private final Collection<T> backing;

    public BindableCollectionImpl(Collection<T> backing) {
        this.backing = backing;
    }

    @Override
    public CollectionInsertResult<T> add(@NonNull T value) {
        final boolean add = this.backing.add(value);
        if (add) {
            return CollectionInsertResult.success(this.internalPostInsert(value));
        }

        return (CollectionInsertResult<T>) CollectionInsertResult.EMPTY;
    }

    @Override
    protected void internalHandleRemove(T value) {
        this.backing.remove(value);
        super.internalHandleRemove(value);
    }

    @SafeVarargs
    @Override
    public final CollectionInsertResult addAll(T... values) {
        final List<Entry<T, Bindable>> affected = new ArrayList<>();
        for (final T value : values) {
            if (this.backing.add(value)) {
                affected.add(this.internalPostInsert(value));
            }
        }

        return CollectionInsertResult.success(affected.toArray(new Entry[0]));
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
    public boolean contains(Object o) {
        return this.backing.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return new BackingIterator<T>(this.backing.iterator(), Function.identity());
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return this.backing.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        if (this.backing.remove(o)) {
            this.internalPostRemove((T) o);
            return true;
        }

        return false;
    }

    @Override
    public boolean containsAll(Collection<? super T> c) {
        return this.backing.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<? super T> c) {
        boolean changed = false;

        for (final Object value : c) {
            changed |= this.remove(value);
        }

        return changed;
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        boolean changed = false;

        for (final T value : this) {
            if (filter.test(value)) {
                changed |= this.remove(value);
            }
        }

        return changed;
    }

    @Override
    public boolean retainAll(Collection<? super T> c) {
        boolean changed = false;

        for (final T value : this) {
            if (!c.contains(value)) {
                changed |= this.remove(value);
            }
        }

        return changed;
    }

    @Override
    public void clear() {
        for (final T value : this) {
            this.internalPostRemove(value);
        }

        this.backing.clear();
    }

    @Override
    protected CollectionReference<T> wrap(T value) {
        return this.backing instanceof List ? CollectionReference.identity(value) : CollectionReference.hashCode(value);
    }
}
