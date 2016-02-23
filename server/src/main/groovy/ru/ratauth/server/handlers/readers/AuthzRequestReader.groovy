package ru.ratauth.server.handlers.readers

import groovy.transform.CompileStatic
import ratpack.http.Headers
import ratpack.util.MultiValueMap
import ru.ratauth.interaction.AuthzRequest
import ru.ratauth.interaction.AuthzResponseType
import ru.ratauth.interaction.GrantType

import static ru.ratauth.server.handlers.readers.RequestUtil.*

/**
 * @author djassan
 * @since 06/11/15
 */
@CompileStatic
class AuthzRequestReader {
  private static final String RESPONSE_TYPE = "response_type"
  private static final String GRANT_TYPE = "grant_type"
  private static final String CLIENT_ID = "client_id"
  private static final String SCOPE = "scope"
  private static final String REDIRECT_URI = "redirect_uri"
  private static final String REFRESH_TOKEN = "refresh_token"
  private static
  final Set<String> BASE_FIELDS = new HashSet<String>(Arrays.asList(RESPONSE_TYPE, CLIENT_ID, SCOPE, REDIRECT_URI, REFRESH_TOKEN, GRANT_TYPE));

  static AuthzRequest readAuthzRequest(MultiValueMap<String, String> params, Headers headers) {
    AuthzResponseType responseType = AuthzResponseType.valueOf(extractField(params, RESPONSE_TYPE, true).toUpperCase())
    GrantType grantType = extractGrantType(params)

    def builder = AuthzRequest.builder()
        .responseType(responseType)
        .scopes(extractField(params, SCOPE, true).split(" ").toList())
        .redirectURI(extractField(params, REDIRECT_URI, false))

    if (GrantType.AUTHENTICATION_TOKEN == grantType) {
      if (responseType == AuthzResponseType.TOKEN) {
        throw new ReadRequestException("Response for that grant_type could not contain token")
      }
      def auth = extractAuth(headers)
      builder.clientId(auth[0])
          .clientSecret(auth[1])
      builder.refreshToken(extractField(params, REFRESH_TOKEN, true))
      builder.externalClientId(extractField(params, CLIENT_ID, true))
      builder.grantType(grantType)
    } else if (responseType == AuthzResponseType.TOKEN) {
      def auth = extractAuth(headers)
      builder.clientId(auth[0])
          .clientSecret(auth[1])
    } else {
      builder.clientId(extractField(params, CLIENT_ID, true))
    }
    builder.authData(extractRest(params, BASE_FIELDS))
    builder.build()
  }

  private static GrantType extractGrantType(MultiValueMap<String,String> params) {
    String grantType = extractField(params, GRANT_TYPE, false);
    if(grantType)
      GrantType.valueOf(grantType.toUpperCase())
    else
      null
  }

}
