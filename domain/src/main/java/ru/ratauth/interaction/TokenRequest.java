package ru.ratauth.interaction;

import lombok.*;

import java.util.Set;

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
  private @Singular Set<String> auds;
}
