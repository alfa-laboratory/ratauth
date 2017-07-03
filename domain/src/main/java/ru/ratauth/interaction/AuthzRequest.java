package ru.ratauth.interaction;

import lombok.*;

import java.util.Map;
import java.util.Set;

/**
 * @author djassan
 * @since 05/11/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthzRequest {
  private AuthzResponseType responseType;
  private @Singular Set<String> scopes;
  private @Singular("acr") Set<String> authContext;
  private String clientId;
  private String clientSecret;
  private String redirectURI;
  private Map<String, String> authData;
  //fields for cross-authorization
  private GrantType grantType;
  private String refreshToken;
  private String sessionToken;
  private String externalClientId;

}
