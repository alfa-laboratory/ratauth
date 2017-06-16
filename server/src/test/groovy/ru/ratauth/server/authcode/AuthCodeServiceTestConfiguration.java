package ru.ratauth.server.authcode;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import ru.ratauth.server.date.DateService;
import ru.ratauth.server.date.DateServiceTestConfiguration;

@TestConfiguration
@EnableConfigurationProperties(AuthCodeProperties.class)
@Import(DateServiceTestConfiguration.class)
public class AuthCodeServiceTestConfiguration {

    @Bean
    public AuthCodeService authCodeService(AuthCodeProperties authCodeProperties, DateService dateService) {
        return new AuthCodeServiceImpl(authCodeProperties, dateService);
    }
}
