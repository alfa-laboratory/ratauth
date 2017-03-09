package ru.ratauth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ratpack.spring.config.EnableRatpack;
import ru.ratauth.server.configuration.OpenIdConnectDiscoveryProperties;
import ru.ratauth.server.configuration.RatAuthProperties;

@Configuration
@EnableRatpack
@ComponentScan("ru.ratauth.server")
@EnableConfigurationProperties({RatAuthProperties.class, OpenIdConnectDiscoveryProperties.class})
public class RatAuthAutoConfiguration {

}
