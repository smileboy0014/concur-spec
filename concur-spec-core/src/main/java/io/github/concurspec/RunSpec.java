package io.github.concurspec;

import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Immutable specification for a single {@link ConcurRunner} execution.
 *
 * <p>
 * {@code RunSpec} defines <b>how a concurrency test should be executed</b>,
 * including thread count, execution duration, timeouts, failure thresholds,
 * and the task to be executed concurrently.
 * </p>
 *
 * <p>
 * Instances are created via the {@link Builder}, which performs validation
 * to ensure only safe and meaningful configurations can be constructed.
 * </p>
 *
 * <p>
 * This type is intentionally immutable and thread-safe, allowing it to be
 * freely shared across worker threads during a test run.
 * </p>
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
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int threads = 16;
        private Duration duration = Duration.ofSeconds(2);
        private Duration totalTimeout = Duration.ofSeconds(10);
        private String threadNamePrefix = "concur";
        private int maxPendingFailures = 0;
        private Runnable task;
        private final ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();

        public Builder threads(int v) {
            this.threads = v;
            return this;
        }

        public Builder duration(Duration v) {
            this.duration = v;
            return this;
        }

        public Builder totalTimeout(Duration v) {
            this.totalTimeout = v;
            return this;
        }

        public Builder threadNamePrefix(String v) {
            this.threadNamePrefix = v;
            return this;
        }

        public Builder maxPendingFailures(int v) {
            this.maxPendingFailures = v;
            return this;
        }

        public Builder task(Runnable v) {
            this.task = v;
            return this;
        }

        public RunSpec build() {
            if (threads <= 0) throw new IllegalArgumentException("threads must be > 0");

            if (duration == null || duration.isZero() || duration.isNegative())
                throw new IllegalArgumentException("duration must be positive");

            if (totalTimeout == null || totalTimeout.isZero() || totalTimeout.isNegative())
                throw new IllegalArgumentException("totalTimeout must be positive");

            if (threadNamePrefix == null || threadNamePrefix.isBlank())
                throw new IllegalArgumentException("threadNamePrefix must not be blank");

            if (maxPendingFailures < 0)
                throw new IllegalArgumentException("maxPendingFailures must be >= 0");

            if (task == null) throw new IllegalArgumentException("task must not be null");

            return new RunSpec(threads, duration, totalTimeout, threadNamePrefix, maxPendingFailures, task, errors);
        }
    }
}
