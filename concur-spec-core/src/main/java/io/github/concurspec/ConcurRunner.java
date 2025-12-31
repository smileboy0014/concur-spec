package io.github.concurspec;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * Minimal concurrent test orchestrator.
 * - All threads start together (barrier)
 * - Run user task until duration elapses or cancelled
 * - Collect success/failure counts, errors, and latency histogram
 */
public final class ConcurRunner {
    private static final AtomicInteger THREAD_ID = new AtomicInteger();

    private ConcurRunner() {
    }

    public static void run(RunSpec spec) throws InterruptedException {
        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(spec.task(), "task");

        int n = spec.threads();

        ExecutorService pool = Executors.newFixedThreadPool(n, r -> {
            Thread t = new Thread(r);
            t.setName(spec.threadNamePrefix() + "-" + THREAD_ID.incrementAndGet());
            t.setDaemon(true);
            t.setUncaughtExceptionHandler((th, ex) -> spec.errors().add(ex));
            return t;
        });

        CyclicBarrier startBarrier = new CyclicBarrier(n + 1);
        CountDownLatch doneLatch = new CountDownLatch(n);

        AtomicBoolean cancel = new AtomicBoolean(false);
        LongAdder success = new LongAdder();
        LongAdder failure = new LongAdder();

        for (int i = 0; i < n; i++) {
            pool.submit(() -> {
                try {
                    startBarrier.await(); // synchronize start
                    final long endAt = System.nanoTime() + spec.duration().toNanos();

                    while (!cancel.get() && System.nanoTime() < endAt) {
                        try {
                            spec.task().run();
                            success.increment();
                        } catch (Throwable t) {
                            failure.increment();
                            spec.errors().add(t);

                            if (spec.maxPendingFailures() > 0 &&
                                    spec.errors().size() >= spec.maxPendingFailures()) {
                                cancel.set(true); // fail-fast
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    spec.errors().add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Release workers
        try {
            startBarrier.await();
        } catch (BrokenBarrierException e) {
            pool.shutdownNow();
            throw new RuntimeException("start barrier broken", e);
        }

        boolean finished = doneLatch.await(spec.totalTimeout().toMillis(), TimeUnit.MILLISECONDS);
        pool.shutdown();

        if (!finished) {
            cancel.set(true);
            pool.shutdownNow();
            throw new TimeoutException("concurrency test timed out after " + spec.totalTimeout());
        }

        System.out.println(
                "ConcurRunner finished: " +
                        "success=" + success.sum() +
                        ", failure=" + failure.sum() +
                        ", errors=" + spec.errors()
        );
    }

    public static final class TimeoutException extends RuntimeException {
        public TimeoutException(String message) {
            super(message);
        }
    }
}
