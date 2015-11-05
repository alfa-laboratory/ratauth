package ru.ratauth.server.handlers.readers

import ratpack.http.Request
import ratpack.util.MultiValueMap
import ru.ratauth.interaction.AuthzRequest
import ru.ratauth.interaction.AuthzResponseType
import ru.ratauth.server.utils.StringUtils

/**
 * @author djassan
 * @since 06/11/15
 */
class AuthzRequestReader {
  private static final String RESPONSE_TYPE = "response_type"
  private static final String CLIENT_ID = "client_id"
  private static final String SCOPE = "scope"
  private static final String REDIRECT_URI = "redirect_uri"

  static AuthzRequest readAuthzRequest(MultiValueMap<String,String> params) {
    AuthzRequest.builder()
    .responseType(AuthzResponseType.valueOf(extractField(params, RESPONSE_TYPE, true).toUpperCase()))
    .clientId(extractField(params, CLIENT_ID, true))
    .scopes(extractField(params, SCOPE, true).split(" ").toList())
    .redirectURI(extractField(params, REDIRECT_URI, false))
    .build()
  }

  private static String extractField(MultiValueMap<String, String> params, String name, Boolean required) {
    String value = params.get(name)
    if(StringUtils.isBlank(value) && required)
      throw new ReadRequestException(name)
    value
  }
}
