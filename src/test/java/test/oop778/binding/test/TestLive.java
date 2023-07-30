package test.oop778.binding.test;

import dev.oop778.bindings.Bindings;
import dev.oop778.bindings.type.Bindable;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import lombok.SneakyThrows;

public class TestLive {
    private final List<Bindable> bindables = new CopyOnWriteArrayList<>();
    private final CountDownLatch latch;

    private static final int NUMBER_OF_BINDABLES = 100;
    private static final int NUMBER_OF_ITERATIONS = 20;
    private static final int THREADS = 10;

    private TestLive() {
        this.latch = new CountDownLatch(THREADS);
        for (int i = 0; i < THREADS; i++) {
            final int finalI = i;
            new Thread(() -> {
                for (int i1 = 0; i1 < NUMBER_OF_ITERATIONS; i1++) {
                    try {
                        this.tick(finalI);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                this.latch.countDown();
            }, "Thread #" + i).start();
        }
    }

    private void tick(int thread) {
        for (int i = 0; i < NUMBER_OF_BINDABLES; i++) {
            // Add new bindable
            if (ThreadLocalRandom.current().nextBoolean()) {
                this.bindables.add(Bindable.create());
            }

            // Bind to random bindable
            if (ThreadLocalRandom.current().nextBoolean()) {
                final Bindable what = this.bindables.get(ThreadLocalRandom.current().nextInt(this.bindables.size() - 1));
                final Bindable to = this.bindables.get(ThreadLocalRandom.current().nextInt(this.bindables.size() - 1));
                if (to != what) {
                    what.bindTo(to);
                }
            }

            // Close random bindable
            if (ThreadLocalRandom.current().nextBoolean()) {
                final Bindable what = this.bindables.get(ThreadLocalRandom.current().nextInt(this.bindables.size() - 1));
                what.close();
            }
        }
    }

    public static void main(String[] args) {
        start();
    }

    @SneakyThrows
    public static void start() {
        final TestLive testLive = new TestLive();
        testLive.latch.await();

        Bindings.getInstance().dumpToFile(Paths.get(System.getProperty("user.dir")).resolve("dump.html").toFile());
    }
}
