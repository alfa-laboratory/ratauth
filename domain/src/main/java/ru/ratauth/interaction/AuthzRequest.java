package ru.ratauth.interaction;

import lombok.*;
import ru.ratauth.entities.AcrValues;
import ru.ratauth.providers.auth.dto.VerifyResult;

import java.util.Map;
import java.util.Optional;
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
  private AcrValues acrValues;
  private String enroll;
  private String clientId;
  private String clientSecret;
  private String redirectURI;
  private Map<String, String> authData;
  //fields for cross-authorization
  private GrantType grantType;
  private String refreshToken;
  private String sessionToken;
  private String externalClientId;

    public VerifyResult addVerifyResultAcrToRequest(VerifyResult verifyResult) {
        AcrValues merged = Optional.ofNullable(verifyResult.getAcrValues())
                .map(verifyAcrValues -> this.getAcrValues().add(verifyAcrValues.getFirst()))
                .orElseGet(this::getAcrValues);
        this.setAcrValues(merged);
        return verifyResult;
    }
}
