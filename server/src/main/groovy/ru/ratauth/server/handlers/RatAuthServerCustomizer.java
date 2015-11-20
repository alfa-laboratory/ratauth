package ru.ratauth.server.handlers;

import org.springframework.stereotype.Component;
import ratpack.func.Action;
import ratpack.guice.BindingsSpec;
import ratpack.handling.Chain;
import ratpack.server.ServerConfigBuilder;
import ratpack.spring.config.RatpackServerCustomizer;

import java.util.Collections;
import java.util.List;

/**
 * @author mgorelikov
 * @since 19/11/15
 */
@Component
public class RatAuthServerCustomizer implements RatpackServerCustomizer {
  @Override
  public List<Action<Chain>> getHandlers() {
    return Collections.emptyList();
  }

  @Override
  public Action<BindingsSpec> getBindings() {
    return spec -> {
      spec.bind(AuthErrorHandler.class);
    };
  }

  @Override
  public Action<ServerConfigBuilder> getServerConfig() {
    return server -> {
    };
  }
}
