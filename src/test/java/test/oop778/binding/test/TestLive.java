package test.oop778.binding.test;

import dev.oop778.bindings.type.Bindable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import lombok.SneakyThrows;

public class TestLive {
    private final List<Bindable> bindables = new CopyOnWriteArrayList<>();
    private final CountDownLatch latch;

    private static final int NUMBER_OF_BINDABLES = 100;
    private static final int NUMBER_OF_ITERATIONS = 50;
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
        final int amountToAdd = ThreadLocalRandom.current().nextInt(0, NUMBER_OF_BINDABLES);
        for (int i = 0; i < amountToAdd; i++) {
            this.bindables.add(Bindable.create());
        }

        if (!this.bindables.isEmpty()) {
            final int amountToBind = ThreadLocalRandom.current().nextInt(0, NUMBER_OF_BINDABLES);
            for (int i = 0; i < amountToBind; i++) {
                final Bindable bindable = this.bindables.get(ThreadLocalRandom.current().nextInt(0, this.bindables.size()));
                final Bindable bindable1 = this.bindables.get(ThreadLocalRandom.current().nextInt(0, this.bindables.size()));
                if (bindable1 == bindable) {
                    continue;
                }

                bindable.bindTo(bindable1);
            }

            final int amountToClose = ThreadLocalRandom.current().nextInt(0, NUMBER_OF_BINDABLES);
            for (int i = 0; i < amountToClose; i++) {
                final Bindable bindable = this.bindables.get(ThreadLocalRandom.current().nextInt(0, this.bindables.size()));
                bindable.close();
                this.bindables.remove(bindable);
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

        //Bindings.getInstance().dumpToFile(.toFile());
    }
}
