package ru.ratauth.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.StringJoiner;

/**
 * @author djassan
 * @since 06/11/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthzResponse {
  private String code;
  private Long expiresIn;
  private String location;
  private String token;

  public String buildURL() {
    StringJoiner joiner = new StringJoiner("&");
    if(code != null && !code.isEmpty()) {
      joiner.add("code="+code);
    }
    if(expiresIn != null) {
      joiner.add("expires_in="+expiresIn);
    }
    if(token != null && !token.isEmpty()) {
      joiner.add("token="+token);
    }
    return location + "?" + joiner.toString();
  }
}
