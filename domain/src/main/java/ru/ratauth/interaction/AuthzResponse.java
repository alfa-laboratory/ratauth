package ru.ratauth.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ratauth.utils.StringUtils;

import java.util.Map;
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
  //intermediate step data for two step authentication
  private Map<String,Object> data;
  private Long expiresIn;
  private String location;
  private String token;
  private TokenType tokenType;
  private String refreshToken;
  private String idToken;

  public String buildURL() {
    StringJoiner joiner = new StringJoiner("&");
    if(!StringUtils.isBlank(code)) {
      joiner.add("code="+code);
    }
    if(expiresIn != null) {
      joiner.add("expires_in="+expiresIn);
    }
    if(!StringUtils.isBlank(token)) {
      joiner.add("token="+token);
      joiner.add("token_type="+tokenType);

    }
    if(!StringUtils.isBlank(refreshToken)) {
      joiner.add("refresh_token="+refreshToken.toString());
    }
    if(!StringUtils.isBlank(idToken)) {
      joiner.add("id_token="+idToken.toString());
    }
    if(data != null && !data.isEmpty()) {
      data.entrySet().forEach(entry -> joiner.add(entry.getKey() + "=" + entry.getValue().toString()));
    }
    return location + "?" + joiner.toString();
  }
}
