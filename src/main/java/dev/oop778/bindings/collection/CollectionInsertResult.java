package dev.oop778.bindings.collection;

import dev.oop778.bindings.type.Bindable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.NonNull;

public interface CollectionInsertResult<T> {
    static CollectionInsertResult<Object> EMPTY = ArrayList::new;

    static <T> CollectionInsertResult<T> success(Map.Entry<T, Bindable>... entries) {
        return () -> Arrays.asList(entries);
    }

    default void bindAllTo(@NonNull Bindable bindable) {
        for (final Map.Entry<T, Bindable> entry : this.getAffected()) {
            entry.getValue().bindTo(bindable);
        }
    }

    Collection<Map.Entry<T, Bindable>> getAffected();

    default void bindAllToSelf() {
        this.bindAllToSelf(null);
    }

    default void bindAllToSelf(@Nullable Bindable fallback) {
        for (final Map.Entry<T, Bindable> entry : this.getAffected()) {
            if (entry.getKey() instanceof Bindable) {
                entry.getValue().bindTo((Bindable) entry.getKey());
                continue;
            }

            if (fallback == null) {
                throw new IllegalStateException("Cannot bind to non-bindable object " + entry.getKey().getClass().getName());
            }

            entry.getValue().bindTo(fallback);
        }
    }

    default boolean wasChanged() {
        return !this.getAffected().isEmpty();
    }
}
