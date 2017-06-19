package ru.ratauth.server.acr;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import ru.ratauth.server.jwt.JWTConfiguration;
import ru.ratauth.server.jwt.JWTDecoder;

@TestConfiguration
@Import(JWTConfiguration.class)
public class MfaTokenAcrMatcherTestConfiguration {

    @Bean
    public AcrMatcher acrMatcher(JWTDecoder jwtDecoder) {
        return new MfaTokenAcrMatcher(jwtDecoder);
    }

}
