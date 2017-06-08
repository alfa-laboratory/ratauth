package ru.ratauth.server.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("ru.ratauth.jwt")
public class JWTProperties {

    private String issuer;
    private String secret;
    private String privateKey;
    private String publicKey;

}
