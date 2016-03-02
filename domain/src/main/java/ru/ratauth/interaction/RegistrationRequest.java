package ru.ratauth.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  private AuthzResponseType responseType;
  private GrantType grantType;
  private Map<String,String> data;
  private Set<String> scopes;
}
