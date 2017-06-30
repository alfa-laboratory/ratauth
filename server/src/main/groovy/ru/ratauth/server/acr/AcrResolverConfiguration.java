package ru.ratauth.server.acr;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AcrResolverConfiguration {

    @Bean
    public AcrResolver acrResolver() {
        return new AcrResolver(defaultAcrMatcher());
    }

    @Bean
    public DefaultAcrMatcher defaultAcrMatcher() {
        return new DefaultAcrMatcher();
    }


}
