package ru.ratauth.server.handlers.readers

import groovy.transform.CompileStatic
import ratpack.http.Headers
import ratpack.util.MultiValueMap
import ru.ratauth.interaction.AuthzRequest
import ru.ratauth.interaction.AuthzResponseType
import static ru.ratauth.server.utils.RequestUtil.*

/**
 * @author djassan
 * @since 06/11/15
 */
@CompileStatic
class AuthzRequestReader {
  private static final String RESPONSE_TYPE = "response_type"
  private static final String CLIENT_ID = "client_id"
  private static final String SCOPE = "scope"
  private static final String REDIRECT_URI = "redirect_uri"
  private static final String AUD = "aud"
  private static final Set<String> BASE_FIELDS = new HashSet<String>(Arrays.asList(RESPONSE_TYPE, CLIENT_ID, SCOPE, REDIRECT_URI, AUD));

  static AuthzRequest readAuthzRequest(MultiValueMap<String, String> params, Headers headers) {
    AuthzResponseType responseType = AuthzResponseType.valueOf(extractField(params, RESPONSE_TYPE, true).toUpperCase())
    def builder = AuthzRequest.builder()
        .responseType(responseType)
        .scopes(extractField(params, SCOPE, true).split(" ").toList())
        .auds(extractField(params, AUD, false)?.split(" ")?.toList())
        .redirectURI(extractField(params, REDIRECT_URI, false))

    if (responseType == AuthzResponseType.TOKEN) {
      def auth = extractAuth(headers)
      builder.clientId(auth[0])
          .clientSecret(auth[1])
    } else {
      builder.clientId(extractField(params, CLIENT_ID, true))
    }
    builder.authData(extractRest(params, BASE_FIELDS))
    builder.build()
  }

}
