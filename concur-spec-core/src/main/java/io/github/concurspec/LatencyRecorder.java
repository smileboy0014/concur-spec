package io.github.concurspec;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Very light log2-bucket histogram (nanoseconds).
 * Not super precise but cheap and GC-friendly for tests.
 */
final class LatencyRecorder {
    // 0..63 buckets for 2^k ranges. bucket k stores counts for (2^k .. 2^(k+1)] ns
    private final LongAdder[] buckets = new LongAdder[64];
    private final AtomicLong min = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong max = new AtomicLong(Long.MIN_VALUE);
    private final LongAdder count = new LongAdder();
    private final LongAdder sum = new LongAdder();

    LatencyRecorder() {
        for (int i = 0; i < buckets.length; i++) buckets[i] = new LongAdder();
    }

    void record(long nanos) {
        if (nanos <= 0) nanos = 1;
        int bucket = 63 - Long.numberOfLeadingZeros(nanos);
        buckets[bucket].increment();
        count.increment();
        sum.add(nanos);
        min.accumulateAndGet(nanos, Math::min);
        max.accumulateAndGet(nanos, Math::max);
    }

    LatencySnapshot snapshot() {
        long[] cs = new long[buckets.length];
        long total = 0;
        for (int i = 0; i < buckets.length; i++) {
            long v = buckets[i].sum();
            cs[i] = v;
            total += v;
        }
        return new LatencySnapshot(cs, total, min.get() == Long.MAX_VALUE ? 0 : min.get(), max.get(), sum.sum());
    }
}
