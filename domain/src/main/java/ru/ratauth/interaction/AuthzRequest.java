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
public class AuthzRequest {
  private AuthzResponseType responseType;
  private @Singular Set<String> scopes;
  private String clientId;
  private String redirectURI;
}
