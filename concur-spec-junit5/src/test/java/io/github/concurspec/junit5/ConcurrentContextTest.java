package io.github.concurspec.junit5;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentContextTest {

    @Test
    @ConcurrentTest(threads = 10, duration = "300ms", warmup = "0ms")
    void repeat_호출시_통계가_생성된다(ConcurrentContext ctx) throws InterruptedException {
        AtomicInteger i = new AtomicInteger();
        ctx.repeat(i::incrementAndGet);

        if (ctx.successCount() <= 0) throw new AssertionError("should have successes");
        ctx.assertNoUncaughtErrors();
    }
}
