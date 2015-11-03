package ru.ratauth.server.handlers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ratauth.interaction.TokenResponse;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenDTO {
  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("token_type")
  private String tokenType;
  @JsonProperty("id_token")
  private String idToken;
  @JsonProperty("expires_in")
  private Long expiresIn;

  public TokenDTO(TokenResponse response) {
    this.accessToken = response.getAccessToken();
    this.tokenType = response.getTokenType();
    this.idToken = response.getIdToken();
    this.expiresIn = response.getExpiresIn();
  }

}
