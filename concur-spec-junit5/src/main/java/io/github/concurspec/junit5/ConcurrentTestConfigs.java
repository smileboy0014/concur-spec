package io.github.concurspec.junit5;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

final class ConcurrentTestConfigs {
    private ConcurrentTestConfigs() {
    }

    static ConcurrentTest find(ExtensionContext ctx) {
        // method annotation wins, then class annotation
        Optional<ConcurrentTest> onMethod = ctx.getTestMethod()
                .flatMap(m -> Optional.ofNullable(m.getAnnotation(ConcurrentTest.class)));
        if (onMethod.isPresent()) return onMethod.get();

        Optional<ConcurrentTest> onClass = ctx.getTestClass()
                .flatMap(c -> Optional.ofNullable(c.getAnnotation(ConcurrentTest.class)));
        return onClass.orElseThrow(() ->
                new IllegalStateException("@ConcurrentTest not found but extension invoked"));
    }
}
