package io.github.concurspec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurRunnerTest {

    @Test
    @DisplayName("spec이 null이면 NPE가 발생한다")
    void shouldThrowNPEWhenSpecIsNull() {
        // given
        RunSpec nullSpec = null;

        // when & then
        assertThrows(NullPointerException.class, () ->
                ConcurRunner.run(nullSpec)
        );
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
        ConcurRunner.run(spec);
        // then
        assertTrue(counter.get() > 0, "Task should have been executed at least once");
        assertTrue(spec.errors().isEmpty(), "No errors should have occurred");
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
        ConcurRunner.run(spec);

        // then
        assertTrue(threadNames.size() > 1, "Multiple threads should have executed");
        threadNames.keySet().forEach(name ->
                assertTrue(name.startsWith("test-thread-"), "Thread name should start with prefix")
        );
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
        ConcurRunner.run(spec);

        // then
        assertFalse(spec.errors().isEmpty(), "Errors should have been collected");
        assertTrue(spec.errors().stream().anyMatch(e -> e.getMessage().equals("Test error")),
                "Test exception should be in errors");
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
        ConcurRunner.run(spec);
        long elapsedTime = System.currentTimeMillis() - startTime;

        // then
        assertTrue(spec.errors().size() >= 5, "Should have at least maxPendingFailures errors");
        assertTrue(elapsedTime < 10000, "Should have terminated early before full duration");
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
        assertThrows(ConcurRunner.TimeoutException.class, () ->
                ConcurRunner.run(spec)
        );
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
        ConcurRunner.run(spec);

        // then
        assertTrue(callCount.get() > 0, "Task should have been called");
        assertFalse(spec.errors().isEmpty(), "Some errors should have been collected");
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
        ConcurRunner.run(spec);
        long elapsedTime = System.currentTimeMillis() - startTime;

        // then
        assertTrue(elapsedTime < 1000, "Should finish quickly with short duration");
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
        ConcurRunner.run(spec);

        // then
        assertTrue(counter.get() > 0, "Task should have been executed");
        assertTrue(spec.errors().isEmpty(), "No errors should have occurred");
    }
}
