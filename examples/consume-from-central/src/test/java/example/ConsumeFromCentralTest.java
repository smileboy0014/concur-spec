package example;

import io.github.concurspec.ConcurRunner;
import io.github.concurspec.RunSpec;
import io.github.concurspec.RunStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class ConsumeFromCentralTest {

    @Test
    @DisplayName("Maven Central artifact로 테스트가 정상 실행된다")
    void shouldRunUsingMavenCentralArtifact() throws InterruptedException {
        RunSpec spec = RunSpec.builder()
                .threads(8)
                .duration(Duration.ofMillis(300))
                .task(() -> {
                    // no-op work
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                })
                .build();

        RunStats result = ConcurRunner.run(spec);

        assertThat(result.successCount()).isGreaterThan(0);
        assertThatCode(result::assertNoUncaughtErrors).doesNotThrowAnyException();
    }
}
