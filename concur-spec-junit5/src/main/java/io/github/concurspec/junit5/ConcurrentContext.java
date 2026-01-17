package io.github.concurspec.junit5;

import io.github.concurspec.ConcurRunner;
import io.github.concurspec.RunSpec;
import io.github.concurspec.RunStats;

import java.time.Duration;
import java.util.Objects;

/**
 * JUnit5-injected context.
 * Usage:
 * ctx.repeat(() -> service.call());
 * ctx.assertNoUncaughtErrors();
 */
public final class ConcurrentContext {

    private final ConcurrentTest cfg;
    private RunStats stats;

    ConcurrentContext(ConcurrentTest cfg) {
        this.cfg = Objects.requireNonNull(cfg, "cfg");
    }

    /**
     * Run the given task concurrently using annotation config.
     */
    public void repeat(Runnable task) throws InterruptedException {
        Objects.requireNonNull(task, "task");

        Duration warmup = DurationParser.parse(cfg.warmup());
        Duration duration = DurationParser.parse(cfg.duration());
        Duration totalTimeout = DurationParser.parse(cfg.totalTimeout());

        if (!warmup.isZero() && warmup.toMillis() > 0) {
            RunSpec warm = RunSpec.builder()
                    .threads(cfg.threads())
                    .duration(warmup)
                    .totalTimeout(totalTimeout)
                    .threadNamePrefix(cfg.threadNamePrefix())
                    .maxPendingFailures(cfg.maxPendingFailures())
                    .task(task)
                    .build();
            // discard
            ConcurRunner.run(warm);
        }

        RunSpec spec = RunSpec.builder()
                .threads(cfg.threads())
                .duration(duration)
                .totalTimeout(totalTimeout)
                .threadNamePrefix(cfg.threadNamePrefix())
                .maxPendingFailures(cfg.maxPendingFailures())
                .task(task)
                .build();

        this.stats = ConcurRunner.run(spec);
    }

    private RunStats requireStats() {
        if (stats == null) throw new IllegalStateException("No run executed. Call ctx.repeat(...) first.");
        return stats;
    }

    // ---- Assertions / accessors ----
    public void assertNoUncaughtErrors() {
        requireStats().assertNoUncaughtErrors();
    }

    public void assertSuccessRateAtLeast(double rate) {
        requireStats().assertSuccessRateAtLeast(rate);
    }

    public void assertNoDeadlock() {
        requireStats().assertNoDeadlock();
    }

    public long successCount() {
        return requireStats().successCount();
    }

    public long failureCount() {
        return requireStats().failureCount();
    }

    public RunStats stats() {
        return requireStats();
    }
}
