package dev.oop778.bindings.util;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Entry<T1, T2> implements Map.Entry<T1, T2> {
    private final T1 key;
    private T2 value;

    public static <T1, T2> Entry<T1, T2> create(T1 first, T2 second) {
        return new Entry<>(first, second);
    }

    public T2 setValue(T2 value) {
        this.value = value;
        return value;
    }
}
