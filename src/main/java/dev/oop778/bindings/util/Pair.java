package dev.oop778.bindings.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair<T1, T2> {
    private final T1 first;
    private final T2 second;

    public static <T1, T2> Pair<T1, T2> create(T1 first, T2 second) {
        return new Pair<>(first, second);
    }
}
