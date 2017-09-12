package ru.ratauth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ratpack.spring.config.EnableRatpack;
import ru.ratauth.server.configuration.OpenIdConnectDefaultDiscoveryProperties;

@Configuration
@EnableRatpack
@ComponentScan("ru.ratauth.server")
@EnableConfigurationProperties(OpenIdConnectDefaultDiscoveryProperties.class)
public class RatAuthAutoConfiguration {

}
