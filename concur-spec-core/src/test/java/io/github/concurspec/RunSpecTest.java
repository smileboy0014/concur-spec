package io.github.concurspec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RunSpecTest {

    @Test
    @DisplayName("기본값으로 RunSpec 생성이 가능하다")
    void shouldCreateRunSpecWithDefaultValues() {
        // given
        Runnable task = () -> {};

        // when
        RunSpec spec = RunSpec.builder()
                .task(task)
                .build();

        // then
        assertNotNull(spec);
        assertEquals(16, spec.threads());
        assertEquals(Duration.ofSeconds(2), spec.duration());
        assertEquals(Duration.ofSeconds(10), spec.totalTimeout());
        assertEquals("concur", spec.threadNamePrefix());
        assertEquals(0, spec.maxPendingFailures());
        assertNotNull(spec.errors());
    }

    @Test
    @DisplayName("threads가 0 이하이면 예외가 발생한다")
    void shouldThrowExceptionWhenThreadsIsZeroOrNegative() {
        // given
        int invalidThreads = 0;
        Runnable task = () -> {};

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                RunSpec.builder()
                        .threads(invalidThreads)
                        .task(task)
                        .build()
        );

        assertTrue(exception.getMessage().contains("threads"));
    }

    @Test
    @DisplayName("duration이 null이면 예외가 발생한다")
    void shouldThrowExceptionWhenDurationIsNull() {
        // given
        Duration nullDuration = null;
        Runnable task = () -> {};

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                RunSpec.builder()
                        .duration(nullDuration)
                        .task(task)
                        .build()
        );

        assertTrue(exception.getMessage().contains("duration"));
    }

    @Test
    @DisplayName("duration이 0 또는 음수이면 예외가 발생한다")
    void shouldThrowExceptionWhenDurationIsZeroOrNegative() {
        // given
        Runnable task = () -> {};

        // when & then - zero duration
        assertThrows(IllegalArgumentException.class, () ->
                RunSpec.builder()
                        .duration(Duration.ZERO)
                        .task(task)
                        .build()
        );

        // when & then - negative duration
        assertThrows(IllegalArgumentException.class, () ->
                RunSpec.builder()
                        .duration(Duration.ofMillis(-1))
                        .task(task)
                        .build()
        );
    }

    @Test
    @DisplayName("totalTimeout이 0 또는 음수이면 예외가 발생한다")
    void shouldThrowExceptionWhenTotalTimeoutIsZeroOrNegative() {
        // given
        Runnable task = () -> {};

        // when & then - zero totalTimeout
        assertThrows(IllegalArgumentException.class, () ->
                RunSpec.builder()
                        .totalTimeout(Duration.ZERO)
                        .task(task)
                        .build()
        );

        // when & then - negative totalTimeout
        assertThrows(IllegalArgumentException.class, () ->
                RunSpec.builder()
                        .totalTimeout(Duration.ofMillis(-1))
                        .task(task)
                        .build()
        );
    }

    @Test
    @DisplayName("threadNamePrefix가 blank이면 예외가 발생한다")
    void shouldThrowExceptionWhenThreadNamePrefixIsBlank() {
        // given
        String blankPrefix = "  ";
        Runnable task = () -> {};

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                RunSpec.builder()
                        .threadNamePrefix(blankPrefix)
                        .task(task)
                        .build()
        );

        assertTrue(exception.getMessage().contains("threadNamePrefix"));
    }

    @Test
    @DisplayName("maxPendingFailures가 음수이면 예외가 발생한다")
    void shouldThrowExceptionWhenMaxPendingFailuresIsNegative() {
        // given
        int negativeValue = -1;
        Runnable task = () -> {};

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                RunSpec.builder()
                        .maxPendingFailures(negativeValue)
                        .task(task)
                        .build()
        );

        assertTrue(exception.getMessage().contains("maxPendingFailures"));
    }

    @Test
    @DisplayName("task가 null이면 예외가 발생한다")
    void shouldThrowExceptionWhenTaskIsNull() {
        // given
        Runnable nullTask = null;

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                RunSpec.builder()
                        .task(nullTask)
                        .build()
        );

        assertTrue(exception.getMessage().contains("task"));
    }

}
