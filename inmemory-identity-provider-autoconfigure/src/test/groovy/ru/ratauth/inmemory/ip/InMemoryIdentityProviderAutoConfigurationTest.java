package ru.ratauth.inmemory.ip;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.ratauth.inmemory.ip.services.InMemorySessionService;
import ru.ratauth.services.SessionService;

import static junit.framework.TestCase.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryIdentityProviderAutoConfigurationTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  private ConfigurableApplicationContext context;

  @Test
  public void whenSessionServiceAlreadyExistsAutoConfigurationIsNotLoaded() {
    context = registerAndRefresh(SessionServiceTestConfiguration.class);

    SessionService sessionService = context.getBean(SessionService.class);

    assertFalse(sessionService instanceof InMemorySessionService);
  }

  @Test
  public void testAutoconfigurationDisabledWhenPropertiesSwitchOff() {
    expectedException.expect(NoSuchBeanDefinitionException.class);
    expectedException.expectMessage("ru.ratauth.services.SessionService");

    context = registerAndRefresh(DefaultTestConfiguration.class, "ru.ratauth.inmemory.ip.enabled=false");

    assertThat(context.getBean(SessionService.class)).isNull();
  }

  public AnnotationConfigApplicationContext registerAndRefresh(Class<?> clazz, String... properties) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    EnvironmentTestUtils.addEnvironment(context, properties);
    context.register(clazz);
    context.refresh();
    return context;
  }

}
