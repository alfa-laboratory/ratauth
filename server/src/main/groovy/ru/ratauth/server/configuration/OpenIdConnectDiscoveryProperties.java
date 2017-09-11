package ru.ratauth.server.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.ratauth.entities.DiscoveryProperties;

import java.util.List;

/**
 * @author tolkv
 * @version 09/11/2016
 */
@Data
@ConfigurationProperties(prefix = "openid.properties.discovery")
public class OpenIdConnectDiscoveryProperties implements DiscoveryProperties {
  // Required
  private String issuer;
  private String jwksUri;
  private List<String> responseTypesSupported;
  private List<String> subjectTypesSupported;
  private List<String> tokenEndpointAuthSigningAlgValuesSupported;

  // Optional
  private String authorizationEndpoint;
  private String tokenEndpoint;
  private String checkSessionIframe;
  private String endSessionEndpoint;

  // Recommended
  private String userInfoEndpoint;
  private String registrationEndpoint;
  private List<String> scopesSupported;
  private List<String> claimsSupported;
}
