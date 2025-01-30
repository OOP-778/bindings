package dev.oop778.bindings.util.map;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class IdentityMap<KEY, VALUE> {
    private final Map<Wrapped<KEY>, VALUE> map = new ConcurrentHashMap<>();

    public VALUE compute(KEY key, BiFunction<KEY, VALUE, VALUE> remapper) {
        return this.map.compute(new Wrapped<>(key), (wrappedKey, oldValue) ->
                remapper.apply(key, oldValue)
        );
    }

    public VALUE put(KEY key, VALUE value) {
        return this.map.put(new Wrapped<>(key), value);
    }

    public VALUE get(KEY key) {
        return this.map.get(new Wrapped<>(key));
    }

    public VALUE getIfAbsent(KEY key, Supplier<VALUE> supplier) {
        return this.map.computeIfAbsent(new Wrapped<>(key), ($) -> supplier.get());
    }

    public VALUE remove(KEY key) {
        return this.map.remove(new Wrapped<>(key));
    }

    public int size() {
        return this.map.size();
    }

    public Collection<VALUE> values() {
        return this.map.values();
    }

    public static class Wrapped<T> {
        private final T value;

        public Wrapped(T value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this.value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }

            final Wrapped<?> that = (Wrapped<?>) obj;
            return this.value == that.value;
        }
    }
}