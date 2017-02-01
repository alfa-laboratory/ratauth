package ru.ratauth.server.configuration;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author mgorelikov
 * @since 31/01/17
 */
@Data
@NoArgsConstructor
@ConfigurationProperties("auth.signature")
public class SignatureConfig {
  private String issuer;
  private String masterSecret;
}
