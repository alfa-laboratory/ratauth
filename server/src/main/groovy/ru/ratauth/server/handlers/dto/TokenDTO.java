package ru.ratauth.server.handlers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ratauth.interaction.AuthzResponseType;
import ru.ratauth.interaction.TokenResponse;

import java.util.Set;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenDTO {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("id_token")
    private String idToken;
    @JsonProperty("expires_in")
    private Long expiresIn;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("session_token")
    private String sessionToken;

    public TokenDTO(TokenResponse response, Set<AuthzResponseType> authzResponseTypeSet) {
        this.accessToken = response.getAccessToken();
        this.refreshToken = response.getRefreshToken();
        this.tokenType = response.getTokenType();
        this.idToken = response.getIdToken();
        this.expiresIn = response.getExpiresIn();
        this.clientId = response.getClientId();

        if (authzResponseTypeSet.contains(AuthzResponseType.SESSION_TOKEN)) {
            this.sessionToken = response.getSessionToken();
        }
    }

}
