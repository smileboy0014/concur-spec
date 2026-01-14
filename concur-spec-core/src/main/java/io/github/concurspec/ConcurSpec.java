package io.github.concurspec;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Fluent entry point for running concurrency tests.
 *
 * <p>
 * Hides thread, latch, and executor management and
 * provides a simple DSL to execute tasks concurrently.
 * </p>
 */
public final class ConcurSpec {

    private ConcurSpec() {
    }

    public static RunStats run(Consumer<SpecBuilder> spec) throws InterruptedException {
        Objects.requireNonNull(spec, "spec");

        SpecBuilder b = new SpecBuilder();
        spec.accept(b);

        RunSpec rs = RunSpec.builder()
                .threads(b.threads)
                .duration(b.duration)
                .totalTimeout(b.totalTimeout)
                .threadNamePrefix(b.threadNamePrefix)
                .maxPendingFailures(b.maxPendingFailures)
                .task(Objects.requireNonNull(b.task, "task"))
                .build();

        RunStats stats = ConcurRunner.run(rs);
        if (b.after != null) b.after.accept(stats);
        return stats;
    }

    public static final class SpecBuilder {
        private int threads = 16;
        private Duration duration = Duration.ofSeconds(2);
        private Duration totalTimeout = Duration.ofSeconds(10);
        private String threadNamePrefix = "concur";
        private int maxPendingFailures = 0;
        private Runnable task;
        private Consumer<RunStats> after;

        public SpecBuilder threads(int v) {
            this.threads = v;
            return this;
        }

        public SpecBuilder duration(Duration v) {
            this.duration = v;
            return this;
        }

        public SpecBuilder totalTimeout(Duration v) {
            this.totalTimeout = v;
            return this;
        }

        public SpecBuilder threadNamePrefix(String v) {
            this.threadNamePrefix = v;
            return this;
        }

        public SpecBuilder maxPendingFailures(int v) {
            this.maxPendingFailures = v;
            return this;
        }

        /**
         * main workload
         */
        public SpecBuilder task(Runnable v) {
            this.task = v;
            return this;
        }

        /**
         * post assertions/reporting hook
         */
        public SpecBuilder after(Consumer<RunStats> v) {
            this.after = v;
            return this;
        }
    }
}
