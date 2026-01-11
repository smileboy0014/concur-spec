package io.github.concurspec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class ConcurRunnerTest {

    @Test
    @DisplayName("spec이 null이면 NPE가 발생한다")
    void shouldThrowNPEWhenSpecIsNull() {
        // given
        RunSpec nullSpec = null;

        // when & then
        assertThatThrownBy(() -> ConcurRunner.run(nullSpec))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("정상적으로 task가 실행되고 완료된다")
    void shouldRunTaskSuccessfully() throws InterruptedException {
        // given
        AtomicInteger counter = new AtomicInteger(0);

        RunSpec spec = RunSpec.builder()
                .threads(4)
                .duration(Duration.ofMillis(100))
                .totalTimeout(Duration.ofSeconds(5))
                .task(counter::incrementAndGet)
                .build();

        // when
        RunStats result = ConcurRunner.run(spec);

        // then
        assertSoftly(softly -> {
            softly.assertThat(counter.get()).isGreaterThan(0);
            softly.assertThat(result.errors()).isEmpty();
            softly.assertThat(result.successCount()).isGreaterThan(0);
            softly.assertThat(result.failureCount()).isEqualTo(0);
            softly.assertThat(result.latency()).isNotNull();
            softly.assertThat(result.latency().count()).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("여러 스레드가 동시에 실행된다")
    void shouldRunMultipleThreadsConcurrently() throws InterruptedException {
        // given
        ConcurrentHashMap<String, Boolean> threadNames = new ConcurrentHashMap<>();
        RunSpec spec = RunSpec.builder()
                .threads(8)
                .duration(Duration.ofMillis(50))
                .totalTimeout(Duration.ofSeconds(5))
                .threadNamePrefix("test-thread")
                .task(() -> threadNames.put(Thread.currentThread().getName(), true))
                .build();

        // when
        RunStats result = ConcurRunner.run(spec);

        // then
        assertSoftly(softly -> {
            softly.assertThat(threadNames).hasSizeGreaterThan(1);
            softly.assertThat(threadNames.keySet()).allMatch(name -> name.startsWith("test-thread-"));
            softly.assertThat(result.successCount()).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("task가 예외를 던지면 errors에 수집된다")
    void shouldCollectErrorsWhenTaskThrowsException() throws InterruptedException {
        // given
        RuntimeException testException = new RuntimeException("Test error");

        RunSpec spec = RunSpec.builder()
                .threads(2)
                .duration(Duration.ofMillis(50))
                .totalTimeout(Duration.ofSeconds(5))
                .task(() -> {
                    throw testException;
                })
                .build();

        // when
        RunStats result = ConcurRunner.run(spec);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.errors()).isNotEmpty();
            softly.assertThat(result.errors()).anyMatch(e -> e.getMessage().equals("Test error"));
            softly.assertThat(result.successCount()).isEqualTo(0);
            softly.assertThat(result.failureCount()).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("maxPendingFailures에 도달하면 조기 종료된다")
    void shouldStopEarlyWhenMaxPendingFailuresReached() throws InterruptedException {
        // given
        AtomicInteger executionCount = new AtomicInteger(0);

        RunSpec spec = RunSpec.builder()
                .threads(4)
                .duration(Duration.ofSeconds(10)) // 긴 duration 설정
                .totalTimeout(Duration.ofSeconds(15))
                .maxPendingFailures(5)
                .task(() -> {
                    executionCount.incrementAndGet();
                    throw new RuntimeException("Intentional failure");
                })
                .build();

        // when
        long startTime = System.currentTimeMillis();
        RunStats result = ConcurRunner.run(spec);
        long elapsedTime = System.currentTimeMillis() - startTime;

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.errors()).hasSizeGreaterThanOrEqualTo(5);
            softly.assertThat(elapsedTime).isLessThan(10000);
            softly.assertThat(result.failureCount()).isGreaterThanOrEqualTo(5);
        });
    }

    @Test
    @DisplayName("totalTimeout을 초과하면 TimeoutException이 발생한다")
    void shouldThrowTimeoutExceptionWhenTotalTimeoutExceeded() {
        // given
        RunSpec spec = RunSpec.builder()
                .threads(2)
                .duration(Duration.ofSeconds(100)) // 매우 긴 duration
                .totalTimeout(Duration.ofMillis(100)) // 짧은 timeout
                .task(() -> {
                    try {
                        Thread.sleep(1000); // task가 오래 걸림
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                })
                .build();

        // when & then
        assertThatThrownBy(() -> ConcurRunner.run(spec))
                .isInstanceOf(ConcurRunner.TimeoutException.class);
    }

    @Test
    @DisplayName("성공과 실패가 혼재된 경우 모두 카운트된다")
    void shouldCountBothSuccessAndFailure() throws InterruptedException {
        // given
        AtomicInteger callCount = new AtomicInteger(0);

        RunSpec spec = RunSpec.builder()
                .threads(4)
                .duration(Duration.ofMillis(100))
                .totalTimeout(Duration.ofSeconds(5))
                .task(() -> {
                    int count = callCount.incrementAndGet();
                    if (count % 2 == 0) {
                        throw new RuntimeException("Even number failure");
                    }
                    // 홀수는 성공
                })
                .build();

        // when
        RunStats result = ConcurRunner.run(spec);

        // then
        assertSoftly(softly -> {
            softly.assertThat(callCount.get()).isGreaterThan(0);
            softly.assertThat(result.errors()).isNotEmpty();
            softly.assertThat(result.successCount()).isGreaterThan(0);
            softly.assertThat(result.failureCount()).isGreaterThan(0);
            softly.assertThat(callCount.get()).isEqualTo(result.successCount() + result.failureCount());
        });
    }

    @Test
    @DisplayName("duration이 짧으면 빠르게 종료된다")
    void shouldFinishQuicklyWithShortDuration() throws InterruptedException {
        // given
        RunSpec spec = RunSpec.builder()
                .threads(4)
                .duration(Duration.ofMillis(50))
                .totalTimeout(Duration.ofSeconds(5))
                .task(() -> {
                })
                .build();

        // when
        long startTime = System.currentTimeMillis();
        RunStats result = ConcurRunner.run(spec);
        long elapsedTime = System.currentTimeMillis() - startTime;

        // then
        assertSoftly(softly -> {
            softly.assertThat(elapsedTime).isLessThan(1000);
            softly.assertThat(result.successCount()).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("스레드 수가 1이어도 정상 동작한다")
    void shouldWorkWithSingleThread() throws InterruptedException {
        // given
        AtomicInteger counter = new AtomicInteger(0);

        RunSpec spec = RunSpec.builder()
                .threads(1)
                .duration(Duration.ofMillis(50))
                .totalTimeout(Duration.ofSeconds(5))
                .task(counter::incrementAndGet)
                .build();

        // when
        RunStats result = ConcurRunner.run(spec);

        // then
        assertSoftly(softly -> {
            softly.assertThat(counter.get()).isGreaterThan(0);
            softly.assertThat(result.errors()).isEmpty();
            softly.assertThat(result.successCount()).isGreaterThan(0);
            softly.assertThat(result.failureCount()).isEqualTo(0);
        });
    }

    @Test
    @DisplayName("latency 통계가 정확히 기록된다")
    void shouldRecordLatencyStatistics() throws InterruptedException {
        // given
        RunSpec spec = RunSpec.builder()
                .threads(4)
                .duration(Duration.ofMillis(100))
                .totalTimeout(Duration.ofSeconds(5))
                .task(() -> {
                    // 약간의 작업 수행
                    for (int i = 0; i < 100; i++) {
                        Math.sqrt(i);
                    }
                })
                .build();

        // when
        RunStats result = ConcurRunner.run(spec);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.latency()).isNotNull();
            softly.assertThat(result.latency().count()).isGreaterThan(0);
            softly.assertThat(result.latency().minNanos()).isGreaterThan(0);
            softly.assertThat(result.latency().maxNanos()).isGreaterThanOrEqualTo(result.latency().minNanos());
            softly.assertThat(result.latency().avgNanos()).isGreaterThan(0);
            softly.assertThat(result.latency().p50Nanos()).isGreaterThan(0);
            softly.assertThat(result.latency().p95Nanos()).isGreaterThan(0);
            softly.assertThat(result.latency().p99Nanos()).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("assertNoUncaughtErrors는 에러가 없으면 성공한다")
    void shouldPassAssertNoUncaughtErrorsWhenNoErrors() throws InterruptedException {
        // given
        RunSpec spec = RunSpec.builder()
                .threads(2)
                .duration(Duration.ofMillis(50))
                .totalTimeout(Duration.ofSeconds(5))
                .task(() -> {
                })
                .build();

        // when
        RunStats result = ConcurRunner.run(spec);

        // then
        assertThatCode(result::assertNoUncaughtErrors).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("assertNoUncaughtErrors는 에러가 있으면 AssertionError를 던진다")
    void shouldThrowAssertionErrorWhenErrorsExist() throws InterruptedException {
        // given
        RunSpec spec = RunSpec.builder()
                .threads(2)
                .duration(Duration.ofMillis(50))
                .totalTimeout(Duration.ofSeconds(5))
                .task(() -> {
                    throw new RuntimeException("Test error");
                })
                .build();

        // when
        RunStats result = ConcurRunner.run(spec);

        // then
        assertThatThrownBy(result::assertNoUncaughtErrors)
                .isInstanceOf(AssertionError.class);
    }

    @Test
    @DisplayName("assertSuccessRateAtLeast는 성공률이 충족되면 성공한다")
    void shouldPassAssertSuccessRateAtLeast() throws InterruptedException {
        // given
        AtomicInteger counter = new AtomicInteger(0);
        RunSpec spec = RunSpec.builder()
                .threads(4)
                .duration(Duration.ofMillis(100))
                .totalTimeout(Duration.ofSeconds(5))
                .task(() -> {
                    int count = counter.incrementAndGet();
                    // 10번 중 9번 성공
                    if (count % 10 == 0) {
                        throw new RuntimeException("Occasional failure");
                    }
                })
                .build();

        // when
        RunStats result = ConcurRunner.run(spec);

        // then
        assertThatCode(() -> result.assertSuccessRateAtLeast(0.8)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("assertSuccessRateAtLeast는 성공률이 미달하면 AssertionError를 던진다")
    void shouldThrowAssertionErrorWhenSuccessRateBelowThreshold() throws InterruptedException {
        // given
        RunSpec spec = RunSpec.builder()
                .threads(2)
                .duration(Duration.ofMillis(50))
                .totalTimeout(Duration.ofSeconds(5))
                .task(() -> {
                    throw new RuntimeException("Always fail");
                })
                .build();

        // when
        RunStats result = ConcurRunner.run(spec);

        // then
        assertThatThrownBy(() -> result.assertSuccessRateAtLeast(0.5))
                .isInstanceOf(AssertionError.class);
    }
}
