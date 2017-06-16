package ru.ratauth.server.jwt;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class JWTPropertiesTestConfiguration {

    @Bean
    public JWTProperties jwtProperties() {
        JWTProperties jwtProperties = mock(JWTProperties.class);
        when(jwtProperties.getIssuer()).thenReturn("alfa-bank");
        when(jwtProperties.getSecret()).thenReturn("secret");
        return jwtProperties;
    }

}
