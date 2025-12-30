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
            return new RunSpec(threads, duration, totalTimeout, threadNamePrefix, maxPendingFailures, task, errors);
        }
    }
}
