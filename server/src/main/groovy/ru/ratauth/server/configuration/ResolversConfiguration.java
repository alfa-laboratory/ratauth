package ru.ratauth.server.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ratauth.providers.auth.Activator;
import ru.ratauth.providers.auth.ActivatorResolver;
import ru.ratauth.providers.auth.Verifier;
import ru.ratauth.providers.auth.VerifierResolver;
import ru.ratauth.server.services.ActivatorResolverWithValidationService;

import java.util.Collections;
import java.util.List;

@Configuration
public class ResolversConfiguration {

    @Bean
    VerifierResolver verifierResolver(List<Verifier> verifiers) {
        return new VerifierResolver(verifiers);
    }

    @Bean
    ActivatorResolver activatorResolver(List<Activator> activators) {
        return new ActivatorResolverWithValidationService(activators, Collections.emptyList());
    }

}
