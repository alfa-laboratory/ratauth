package ru.ratauth.inmemory.ip;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import ru.ratauth.services.SessionService;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Import(DefaultTestConfiguration.class)
public class SessionServiceTestConfiguration {

  @Bean
  public SessionService sessionService() {
    return mock(SessionService.class);
  }

}
