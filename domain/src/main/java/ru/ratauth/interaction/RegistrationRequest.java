package ru.ratauth.interaction;

import lombok.*;

import java.util.Map;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 29/01/16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {
  private String clientId;
  private String clientSecret;
  private String authCode;
  @Singular
  private Set<AuthzResponseType> responseTypes;
  private GrantType grantType;
  private Map<String,String> data;
  @Singular
  private Set<String> scopes;
  private String redirectURI;
}
