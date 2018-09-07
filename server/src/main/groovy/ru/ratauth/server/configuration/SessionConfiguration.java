package ru.ratauth.server.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "auth.session")
public class SessionConfiguration {

    private Boolean needToCheckSession;

    public Boolean isNeedToCheckSession() {
        return needToCheckSession == null || needToCheckSession;
    }
}
