package io.github.concurspec.junit5;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(ConcurrentTestExtension.class)
public @interface ConcurrentTest {
    int threads() default 50;

    String duration() default "3s";          // ex: "500ms", "3s"

    String warmup() default "0ms";           // discard stats

    int maxPendingFailures() default 5;

    String threadNamePrefix() default "concu";

    String totalTimeout() default "10s";     // safety timeout
}
