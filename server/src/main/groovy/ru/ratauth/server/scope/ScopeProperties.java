package ru.ratauth.server.scope;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ru.ratauth.scope")
public class ScopeProperties {

    private String defaultScope;

}