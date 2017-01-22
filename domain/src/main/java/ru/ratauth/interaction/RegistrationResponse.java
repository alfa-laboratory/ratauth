package ru.ratauth.interaction;

import lombok.*;
import ru.ratauth.utils.StringUtils;

import java.net.URLEncoder;
import java.util.StringJoiner;

import static ru.ratauth.utils.URIUtils.appendQuery;

/**
 * @author mgorelikov
 * @since 29/01/16
 */
//TODO Classes like this could be moved from domain to server project
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponse {
  private String redirectUrl;
  private String idToken;
  private String code;
  private String enrollmentId;

  @SneakyThrows
  public String buildURL() {
    StringJoiner joiner = new StringJoiner("&");
    if (!StringUtils.isBlank(code)) {
      joiner.add("code=" + URLEncoder.encode(code, "UTF-8"));
    }
    if (!StringUtils.isBlank(idToken)) {
      joiner.add("id_token=" + URLEncoder.encode(idToken, "UTF-8"));
    }
    if (!StringUtils.isBlank(enrollmentId)) {
      joiner.add("enrollmentId=" + URLEncoder.encode(enrollmentId, "UTF-8"));
    }
    return appendQuery(redirectUrl, joiner.toString());
  }
}
