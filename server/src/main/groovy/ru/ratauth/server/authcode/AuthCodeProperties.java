package ru.ratauth.server.authcode;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ru.ratauth.auth.code")
public class AuthCodeProperties {

    private long expiresInSecond;

}
