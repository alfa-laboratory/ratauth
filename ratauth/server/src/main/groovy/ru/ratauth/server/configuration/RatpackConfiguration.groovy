package ru.ratauth.server.configuration

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.ratauth.server.services.OpenIdDefaultDiscoveryService
import ru.ratauth.services.OpenIdConnectDiscoveryService

/**
 * @author tolkv
 * @since 30/10/15
 */
@Configuration
class RatpackConfiguration {

    @Bean
    @ConditionalOnMissingBean
    OpenIdConnectDiscoveryService getOpenIdConnectDiscoveryService(OpenIdConnectDefaultDiscoveryProperties properties) {
        return new OpenIdDefaultDiscoveryService(properties)
    }
}
