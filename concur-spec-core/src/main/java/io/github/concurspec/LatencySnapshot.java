package io.github.concurspec;

final class LatencySnapshot {
    private final long[] buckets; // counts per log2 bucket
    private final long count;
    private final long min, max, sum;

    LatencySnapshot(long[] buckets, long count, long min, long max, long sum) {
        this.buckets = buckets;
        this.count = count;
        this.min = min;
        this.max = max;
        this.sum = sum;
    }

    long count() {
        return count;
    }

    long minNanos() {
        return min;
    }

    long maxNanos() {
        return max;
    }

    long avgNanos() {
        return count == 0 ? 0 : sum / count;
    }

    long p50Nanos() {
        return percentile(0.50);
    }

    long p95Nanos() {
        return percentile(0.95);
    }

    long p99Nanos() {
        return percentile(0.99);
    }

    long percentile(double p) {
        if (count == 0) {
            return 0;
        }
        long rank = (long) Math.ceil(p * count);
        long cum = 0;
        for (int i = 0; i < buckets.length; i++) {
            cum += buckets[i];
            if (cum >= rank) {
                // upper bound of bucket i
                return (i == 63) ? Long.MAX_VALUE : (1L << (i + 1));
            }
        }
        return Long.MAX_VALUE;
    }
}
