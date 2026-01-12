package io.github.concurspec;

import java.time.Duration;
import java.util.Collection;

/**
 * Aggregated statistics (run statistics) for a single ConcurRunner execution.
 */
public record RunStats(
        long successCount,
        long failureCount,
        Collection<Throwable> errors,
        LatencySnapshot latency
) {
    public void assertNoUncaughtErrors() {
        if (!errors.isEmpty()) {
            Throwable first = errors.iterator().next();
            throw new AssertionError("Uncaught errors: " + errors.size(), first);
        }
    }

    public void assertSuccessRateAtLeast(double rate) {
        long total = successCount + failureCount;
        double r = total == 0 ? 1.0 : (successCount / (double) total);
        if (r < rate) throw new AssertionError("successRate=" + r + " < " + rate);
    }

    public void assertLatencyP95Below(Duration bound) {
        if (latency.p95Nanos() > bound.toNanos())
            throw new AssertionError("p95=" + Duration.ofNanos(latency.p95Nanos()) + " > " + bound);
    }

    /**
     * Check if JVM currently has deadlocked threads.
     */
    public void assertNoDeadlock() {
        var deadlock = DeadlockDetector.findDeadlock();
        if (deadlock.isPresent()) {
            throw new AssertionError(deadlock.get().toPrettyString());
        }
    }
}
