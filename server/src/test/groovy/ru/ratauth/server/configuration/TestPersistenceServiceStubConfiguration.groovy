package ru.ratauth.server.configuration

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import ru.ratauth.providers.auth.ActivatorResolver
import ru.ratauth.providers.auth.VerifierResolver
import ru.ratauth.server.local.PersistenceServiceStubConfiguration
import ru.ratauth.server.providers.DCAIdentityProvider

/**
 * @author mgorelikov
 * @since 25/02/16
 */
@Import(ResolversConfiguration.class)
@TestConfiguration
class TestPersistenceServiceStubConfiguration extends PersistenceServiceStubConfiguration {

    @Bean
    public DCAIdentityProvider getDCAIdentityProvider(ActivatorResolver activatorResolver, VerifierResolver verifierResolver) {
        return new DCAIdentityProvider(activatorResolver, verifierResolver);
    }

}
