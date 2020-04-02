package ru.ratauth.server.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.ratauth.server.local.PersistenceServiceStubConfiguration;

/**
 * @author tolkv
 * @version 23/09/16
 */
@TestConfiguration
@Import({TestProvidersStubConfiguration.class, PersistenceServiceStubConfiguration.class})
@TestPropertySource(properties = {
        "ratpack.port=8080",
        "ratpack.base-dir=file:server/src/main/resources",
        "ratpack.templates-path=templates"
})
public class TestBaseConfiguration {
}
