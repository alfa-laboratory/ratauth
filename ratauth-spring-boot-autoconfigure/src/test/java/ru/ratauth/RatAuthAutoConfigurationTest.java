package ru.ratauth;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ratpack.spring.config.RatpackConfiguration;
import ru.ratauth.server.RatAuthApplication;
import ru.ratauth.server.autoconfig.RatpackSpringEndpointsAutoConfiguration;
import ru.ratauth.server.configuration.OpenIdConnectDiscoveryProperties;
import ru.ratauth.server.configuration.RatAuthProperties;
import ru.ratauth.server.configuration.renderer.RenderedConfiguration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static ru.ratauth.RatAuthAutoConfigurationTest.ManagementServerPropertiesExistsConfiguration.TestManagementServerProperties;

public class RatAuthAutoConfigurationTest {

  private ConfigurableApplicationContext context;

  @Test
  public void testDefaultConfiguration() {
    registerAndRefresh(TestDefaultConfiguration.class);
    assertThat(this.context.getBean(RatAuthApplication.class)).isNotNull();
    assertThat(this.context.getBean(RatpackConfiguration.class)).isNotNull();
    assertThat(this.context.getBean(RenderedConfiguration.class)).isNotNull();
    assertThat(this.context.getBean(RatpackSpringEndpointsAutoConfiguration.class)).isNotNull();
  }

  @Test
  public void testAllRequiredPropertiesAreLoaded() {
    registerAndRefresh(TestDefaultConfiguration.class);
    assertThat(this.context.getBean(RatAuthProperties.class)).isNotNull();
    assertThat(this.context.getBean(OpenIdConnectDiscoveryProperties.class)).isNotNull();
  }

  @Test
  public void testManagementServerPropertiesAlreadyExists() {
    registerAndRefresh(TestDefaultConfiguration.class, ManagementServerPropertiesExistsConfiguration.class);
    assertThat(this.context.getBean(ManagementServerProperties.class)).isNotNull();
    assertThat(this.context.getBean(ManagementServerProperties.class)).isInstanceOf(TestManagementServerProperties.class);
  }

  @After
  public void close() {
    if (context != null) {
      context.close();
    }
  }

  public void registerAndRefresh(Class<?>... classes) {
    context = new SpringApplicationBuilder(classes)
            .web(false)
            .profiles("local")
            .run();
  }

  @Configuration
  public static class ManagementServerPropertiesExistsConfiguration {

    @Bean
    public ManagementServerProperties managementServerProperties() {
      return new TestManagementServerProperties();
    }

    public static class TestManagementServerProperties extends ManagementServerProperties {

    }

  }

}