import io.github.concurspec.ConcurSpec;
import io.github.concurspec.RunStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class DslUsageTest {

    @Test
    @DisplayName("DSL 실행이 정상적으로 동작한다")
    void shouldDlsRun() throws InterruptedException {
        ConcurSpec.run(spec -> spec
                .threads(20)
                .duration(Duration.ofMillis(500))
                .task(() -> {
                    for (int i = 0; i < 10_000; i++) {}
                })
                .after(RunStats::assertNoUncaughtErrors)
        );
    }
}
