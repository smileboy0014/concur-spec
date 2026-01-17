import io.github.concurspec.junit5.ConcurrentContext;
import io.github.concurspec.junit5.ConcurrentTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentExTest {

    private final AtomicInteger counter = new AtomicInteger();

    @Test
    @ConcurrentTest(threads = 30, duration = "500ms", warmup = "100ms", maxPendingFailures = 5)
    void 증가_동시성_테스트(ConcurrentContext ctx) throws InterruptedException {
        ctx.repeat(counter::incrementAndGet);

        ctx.assertNoUncaughtErrors();
        ctx.assertNoDeadlock();
        // 단순 sanity check
        if (counter.get() <= 0) throw new AssertionError("counter must be > 0");
    }
}
