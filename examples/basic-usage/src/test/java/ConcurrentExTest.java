import io.github.concurspec.junit5.ConcurrentContext;
import io.github.concurspec.junit5.ConcurrentTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentExTest {

    private final AtomicInteger counter = new AtomicInteger();

    @Test
    @DisplayName("증가 동시성 테스트")
    @ConcurrentTest(threads = 30, duration = "500ms", warmup = "100ms", maxPendingFailures = 5)
    void shouldIncreaseCountConcurrently(ConcurrentContext ctx) throws InterruptedException {
        ctx.repeat(counter::incrementAndGet);

        ctx.assertNoUncaughtErrors();
        ctx.assertNoDeadlock();
        // 단순 sanity check
        if (counter.get() <= 0) throw new AssertionError("counter must be > 0");
    }
}
