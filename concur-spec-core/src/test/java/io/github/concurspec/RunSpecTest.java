package io.github.concurspec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class RunSpecTest {

    @Test
    @DisplayName("기본값으로 RunSpec 생성이 가능하다")
    void shouldCreateRunSpecWithDefaultValues() {
        // given
        Runnable task = () -> {
        };

        // when
        RunSpec spec = RunSpec.builder()
                .task(task)
                .build();

        // then
        assertSoftly(softly -> {
            softly.assertThat(spec).isNotNull();
            softly.assertThat(spec.threads()).isEqualTo(16);
            softly.assertThat(spec.duration()).isEqualTo(Duration.ofSeconds(2));
            softly.assertThat(spec.totalTimeout()).isEqualTo(Duration.ofSeconds(10));
            softly.assertThat(spec.threadNamePrefix()).isEqualTo("concur");
            softly.assertThat(spec.maxPendingFailures()).isEqualTo(0);
            softly.assertThat(spec.errors()).isNotNull();
        });
    }

    @Test
    @DisplayName("threads가 0 이하이면 예외가 발생한다")
    void shouldThrowExceptionWhenThreadsIsZeroOrNegative() {
        // given
        int invalidThreads = 0;
        Runnable task = () -> {
        };

        // when & then
        assertThatThrownBy(() ->
                RunSpec.builder()
                        .threads(invalidThreads)
                        .task(task)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("threads");
    }

    @Test
    @DisplayName("duration이 null이면 예외가 발생한다")
    void shouldThrowExceptionWhenDurationIsNull() {
        // given
        Duration nullDuration = null;
        Runnable task = () -> {
        };

        // when & then
        assertThatThrownBy(() ->
                RunSpec.builder()
                        .duration(nullDuration)
                        .task(task)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duration");
    }

    @Test
    @DisplayName("duration이 0 또는 음수이면 예외가 발생한다")
    void shouldThrowExceptionWhenDurationIsZeroOrNegative() {
        // given
        Runnable task = () -> {
        };

        // when & then - zero duration
        assertThatThrownBy(() ->
                RunSpec.builder()
                        .duration(Duration.ZERO)
                        .task(task)
                        .build()
        ).isInstanceOf(IllegalArgumentException.class);

        // when & then - negative duration
        assertThatThrownBy(() ->
                RunSpec.builder()
                        .duration(Duration.ofMillis(-1))
                        .task(task)
                        .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("totalTimeout이 0 또는 음수이면 예외가 발생한다")
    void shouldThrowExceptionWhenTotalTimeoutIsZeroOrNegative() {
        // given
        Runnable task = () -> {
        };

        // when & then - zero totalTimeout
        assertThatThrownBy(() ->
                RunSpec.builder()
                        .totalTimeout(Duration.ZERO)
                        .task(task)
                        .build()
        ).isInstanceOf(IllegalArgumentException.class);

        // when & then - negative totalTimeout
        assertThatThrownBy(() ->
                RunSpec.builder()
                        .totalTimeout(Duration.ofMillis(-1))
                        .task(task)
                        .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("threadNamePrefix가 blank이면 예외가 발생한다")
    void shouldThrowExceptionWhenThreadNamePrefixIsBlank() {
        // given
        String blankPrefix = "  ";
        Runnable task = () -> {
        };

        // when & then
        assertThatThrownBy(() ->
                RunSpec.builder()
                        .threadNamePrefix(blankPrefix)
                        .task(task)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("threadNamePrefix");
    }

    @Test
    @DisplayName("maxPendingFailures가 음수이면 예외가 발생한다")
    void shouldThrowExceptionWhenMaxPendingFailuresIsNegative() {
        // given
        int negativeValue = -1;
        Runnable task = () -> {
        };

        // when & then
        assertThatThrownBy(() ->
                RunSpec.builder()
                        .maxPendingFailures(negativeValue)
                        .task(task)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxPendingFailures");
    }

    @Test
    @DisplayName("task가 null이면 예외가 발생한다")
    void shouldThrowExceptionWhenTaskIsNull() {
        // given
        Runnable nullTask = null;

        // when & then
        assertThatThrownBy(() ->
                RunSpec.builder()
                        .task(nullTask)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("task");
    }

}
