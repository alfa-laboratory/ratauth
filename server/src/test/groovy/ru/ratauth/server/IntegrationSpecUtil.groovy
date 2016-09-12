package ru.ratauth.server

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import com.jayway.restassured.response.Header
import org.springframework.http.HttpHeaders

import java.nio.charset.Charset

/**
 * @author djassan
 * @since 11/09/16
 */
class IntegrationSpecUtil {

  public static Header createAuthHeaders(String username, String password) {
    def auth = username + ":" + password;
    def encodedAuth =
      Base64Coder.encode(auth.getBytes(Charset.forName("UTF-8")))
    def authHeader = "Basic " + new String(encodedAuth)
    return new Header(HttpHeaders.AUTHORIZATION, authHeader)
  }
}
