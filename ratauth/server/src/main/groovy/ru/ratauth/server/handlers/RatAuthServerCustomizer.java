package ru.ratauth.server.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ratpack.error.ServerErrorHandler;
import ratpack.func.Action;
import ratpack.guice.BindingsSpec;
import ratpack.handling.Chain;
import ratpack.server.ServerConfigBuilder;
import ratpack.spring.config.RatpackProperties;
import ratpack.spring.config.RatpackServerCustomizer;

import java.util.Collections;
import java.util.List;

/**
 * @author mgorelikov
 * @since 19/11/15
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RatAuthServerCustomizer implements RatpackServerCustomizer {
    private final LoggingModule loggingModule;
    private final AuthErrorHandler errorHandler;
    private final RatpackProperties ratpackProperties;

    @Override
    public List<Action<Chain>> getHandlers() {
        return Collections.emptyList();
    }

    @Override
    public Action<BindingsSpec> getBindings() {
        return spec -> {
            spec.bindInstance(ServerErrorHandler.class, errorHandler);
            spec.module(loggingModule);
        };
    }

    @Override
    public Action<ServerConfigBuilder> getServerConfig() {
        return server -> {
        };
    }
}
