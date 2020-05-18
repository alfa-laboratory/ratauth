package ru.ratauth.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Wither
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String idToken;
    private Long expiresIn;
    private String clientId;
    private String sessionToken;
}
