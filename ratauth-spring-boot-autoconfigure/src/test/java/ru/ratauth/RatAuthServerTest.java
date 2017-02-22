package ru.ratauth;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import ratpack.server.RatpackServer;

import java.net.UnknownHostException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RatAuthServerTest {

  private static final int DEFAULT_RATPACK_PORT = 5050;
  private ConfigurableApplicationContext context;

  public void registerAndRefresh(Class<?> configClass, String... properties) {
    context = new SpringApplicationBuilder(configClass)
            .web(false)
            .properties(properties)
            .profiles("local")
            .run();
  }

  @Test
  public void testRatpackServerBeanShouldBeLoadedAndStarted() {
    registerAndRefresh(TestDefaultConfiguration.class);

    assertThat(this.context.getBean(RatpackServer.class)).isNotNull();
    assertThat(this.context.getBean(RatpackServer.class).isRunning()).isEqualTo(true);
  }

  @Test
  public void testRatpackServerBeanShouldStartsWithDefaultConfiguration() throws UnknownHostException {
    registerAndRefresh(TestDefaultConfiguration.class);

    RatpackServer ratpackServer = this.context.getBean(RatpackServer.class);

    assertThat(ratpackServer.getBindPort()).isEqualTo(DEFAULT_RATPACK_PORT);
  }

  @Test
  public void testRatpackServerBeanShouldCustomizeServerPort() throws UnknownHostException {
    registerAndRefresh(TestDefaultConfiguration.class, "ratpack.port=6060");

    RatpackServer ratpackServer = this.context.getBean(RatpackServer.class);

    assertThat(ratpackServer.getBindPort()).isEqualTo(6060);
  }

  @After
  public void close() {
    if (context != null) {
      context.close();
    }
  }

}