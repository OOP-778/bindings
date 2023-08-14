package dev.oop778.bindings.collection;

public interface CollectionReference<T> {

    static <T> CollectionReference<T> identity(T value) {
        return new Identity<>(value);
    }

    static <T> CollectionReference<T> hashCode(T value) {
        return new HashCode<>(value);
    }

    T get();

    class HashCode<T> implements CollectionReference<T> {
        private final T value;
        private final int hashCode;

        public HashCode(T value) {
            this.value = value;
            this.hashCode = value.hashCode();
        }

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof HashCode && ((HashCode<?>) obj).hashCode == this.hashCode;
        }

        @Override
        public int hashCode() {
            return this.hashCode;
        }
    }

    class Identity<T> implements CollectionReference<T> {
        private final T value;

        public Identity(T get) {
            this.value = get;
        }

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this.value);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Identity && ((Identity<?>) obj).value == this.value;
        }
    }
}
