import io.github.concurspec.junit5.ConcurrentContext;
import io.github.concurspec.junit5.ConcurrentTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class IterationsPerThreadExampleTest {

    @Test
    @DisplayName("threads=10, iterationsPerThread=1이면 총 10번 실행된다")
    @ConcurrentTest(threads = 10, iterationsPerThread = 1, totalTimeout = "5s")
    void shouldRunExactlyThreadsTimes(ConcurrentContext ctx) throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();

        ctx.repeat(counter::incrementAndGet);

        ctx.assertNoUncaughtErrors();
        assertThat(counter.get()).isEqualTo(10);
        assertThat(ctx.successCount()).isEqualTo(10);
        assertThat(ctx.failureCount()).isEqualTo(0);
    }
}
