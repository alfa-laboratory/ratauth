package ru.ratauth.server.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.ratauth.configuration.BaseConfiguration;
import ru.ratauth.configuration.DCAConfiguration;
import ru.ratauth.server.configuration.annotation.NotTest;

/**
 * @author mgorelikov
 * @since 16/01/17
 */
@NotTest
@Import({DCAConfiguration.class, BaseConfiguration.class})
@Configuration
public class ProvidersConfiguration {
}
