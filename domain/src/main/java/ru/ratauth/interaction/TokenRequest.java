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
public class TokenRequest {
    private String authzCode; //optional
    private String refreshToken;//optional
    private String sessionToken;//optional
    private String clientId;
    private String clientSecret;//means password, not secret key for signature or encryption
    private GrantType grantType;
    @Singular
    private Set<AuthzResponseType> responseTypes;
    private @Singular
    Set<String> scopes;
    private Map<String, String> authData;
}
