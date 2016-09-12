package ru.ratauth.server.configuration;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import ru.ratauth.server.configuration.annotation.Cloud;

/**
 * @author mgorelikov
 * @since 12/09/16
 */

@Cloud
@Configuration
@EnableEurekaClient
@EnableRetry
@EnableDiscoveryClient
public class CloudConfiguration {
}
