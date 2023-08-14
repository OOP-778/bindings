package dev.oop778.bindings.collection;

import dev.oop778.bindings.collection.implementation.BindableCollectionImpl;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;
import javax.annotation.CheckReturnValue;
import lombok.NonNull;

public interface BindableCollection<T> extends Iterable<T> {

    @CheckReturnValue
    CollectionInsertResult<T> add(@NonNull T value);

    @CheckReturnValue
    CollectionInsertResult addAll(T... values);

    int size();

    boolean isEmpty();

    boolean contains(T o);

    @Override
    Iterator<T> iterator();

    <T1> T1[] toArray(T1[] a);

    boolean remove(T o);

    boolean containsAll(Collection<? super T> c);

    boolean removeAll(Collection<? super T> c);

    boolean removeIf(Predicate<? super T> filter);

    boolean retainAll(Collection<? super T> c);

    void clear();

    static <T> BindableCollection<T> wrap(@NonNull Collection<T> backing) {
        return new BindableCollectionImpl<>(backing);
    }
}
