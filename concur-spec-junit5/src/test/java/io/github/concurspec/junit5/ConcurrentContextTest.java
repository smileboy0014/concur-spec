package io.github.concurspec.junit5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentContextTest {

    @Test
    @ConcurrentTest(threads = 10, duration = "300ms", warmup = "0ms")
    @DisplayName("repeat 호출시 통계가 생성된다")
    void shouldCreateStatWhenCallContext(ConcurrentContext ctx) throws InterruptedException {
        AtomicInteger i = new AtomicInteger();
        ctx.repeat(i::incrementAndGet);

        if (ctx.successCount() <= 0) throw new AssertionError("should have successes");
        ctx.assertNoUncaughtErrors();
    }
}
