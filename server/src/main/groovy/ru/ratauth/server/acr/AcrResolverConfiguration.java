package ru.ratauth.server.acr;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.ratauth.server.jwt.JWTConfiguration;
import ru.ratauth.server.jwt.JWTDecoder;

@Configuration
@Import(JWTConfiguration.class)
public class AcrResolverConfiguration {

    @Bean
    public AcrResolver acrResolver(JWTDecoder jwtDecoder) {
        return new AcrResolver(defaultAcrMatcher());
    }

    @Bean
    public DefaultAcrMatcher defaultAcrMatcher() {
        return new DefaultAcrMatcher();
    }


}
