# ConcurSpec

A tiny, zero-dependency concurrency test runner for Java.

- ✅ All threads start together (barrier)
- ✅ Duration-based execution & fail-fast on too many errors
- ✅ Built-in success/failure counters & latency p50/p95/p99 (log2 histogram)
- ✅ Pure JDK (Java 17); plug into JUnit5 tests

## Quick Start

```java
RunStats stats = ConcuRunner.run(
  RunSpec.builder()
    .threads(50)
    .duration(Duration.ofSeconds(2))
    .maxPendingFailures(5)
    .task(() -> paymentFacade.pay(cmd))
    .build()
);

stats.assertNoUncaughtErrors();
stats.assertSuccessRateAtLeast(0.99);
stats.assertLatencyP95Below(Duration.ofMillis(20));
