package io.github.concurspec.junit5;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;

public class ConcurrentTestExtension implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Parameter p = parameterContext.getParameter();
        return p.getType().equals(ConcurrentContext.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        ConcurrentTest cfg = ConcurrentTestConfigs.find(extensionContext);
        return new ConcurrentContext(cfg);
    }
}
