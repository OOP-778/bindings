package jmh.dev.oop778.bindings;

import dev.oop778.bindings.type.Bindable;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(time = 1)
@Warmup(time = 1, iterations = 3)
@Threads(50)
@Fork(1)
@State(org.openjdk.jmh.annotations.Scope.Benchmark)
public class ConcurrencyTest {
    private static final Bindable BINDABLE = Bindable.create();

    @Benchmark
    public void test() {
        BINDABLE.bindTo(Bindable.create());
    }
}
