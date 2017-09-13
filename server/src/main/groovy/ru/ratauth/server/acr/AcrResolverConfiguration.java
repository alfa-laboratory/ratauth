package ru.ratauth.server.acr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ratauth.services.ClientService;

@Configuration
public class AcrResolverConfiguration {


    @Bean
    public AcrResolver acrResolver() {
        return new AcrResolver(defaultAcrMatcher());
    }

    @Bean
    public AcrMatcher acrMatcher(ClientService clientService) {
        return new AcrMatcherWithDefaultClientValues(clientService);
    }

    @Bean
    @ConditionalOnMissingBean
    public AcrMatcher defaultAcrMatcher() {
        return new DefaultAcrMatcher();
    }
}
