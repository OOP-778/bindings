package dev.oop778.bindings;

import dev.oop778.bindings.type.BindableOnce;

import java.util.concurrent.atomic.AtomicInteger;

public class TestBindable implements BindableOnce {
    private final AtomicInteger closeCount = new AtomicInteger(0);

    @Override
    public void close() {
        this.closeCount.incrementAndGet();
    }

    public int getCloseCount() {
        return this.closeCount.get();
    }
}
