package ru.ratauth.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author djassan
 * @since 05/11/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenRequest {
  private String authzCode; //optional
  private String refreshToken;//optional
  private String clientId;
  private String clientSecret;
  private GrantType grantType;
  private AuthzResponseType responseType;
}
