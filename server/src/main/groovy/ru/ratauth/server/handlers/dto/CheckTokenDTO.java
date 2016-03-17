package ru.ratauth.server.handlers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.ratauth.interaction.CheckTokenResponse;

import java.util.Set;

/**
 * @author mgorelikov
 * @since 03/11/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckTokenDTO {
  private String jti;
  private @Singular("auds") Set<String> aud;
  private Long exp;
  @JsonProperty("client_id")
  private String clientId;
  @JsonProperty("scope")
  private @Singular Set<String> scopes;

  public CheckTokenDTO(CheckTokenResponse response) {
    this.jti = response.getIdToken();
    this.exp = response.getExpiresIn();
    this.clientId = response.getClientId();
    this.scopes = response.getScopes();
  }
}
