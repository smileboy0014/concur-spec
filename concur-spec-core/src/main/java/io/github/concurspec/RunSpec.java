package io.github.concurspec;

import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Immutable spec for a ConcuRunner run.
 */
public record RunSpec(
        int threads,
        Duration duration,
        Duration totalTimeout,
        String threadNamePrefix,
        int maxPendingFailures,
        Runnable task,
        ConcurrentLinkedQueue<Throwable> errors
) {
}
