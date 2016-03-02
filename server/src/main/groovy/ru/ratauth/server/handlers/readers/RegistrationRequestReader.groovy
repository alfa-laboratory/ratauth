package ru.ratauth.server.handlers.readers

import ratpack.form.Form
import ratpack.http.Headers
import ru.ratauth.interaction.GrantType
import ru.ratauth.interaction.RegistrationRequest

import static ru.ratauth.server.handlers.readers.RequestUtil.*

/**
 * @author mgorelikov
 * @since 29/01/16
 */
class RegistrationRequestReader {
  private static final String CLIENT_ID = "client_id"
  private static final String CODE = "code"
  private static final String GRANT_TYPE = "grant_type"
  private static final String RESPONSE_TYPE = "response_type"
  private static final Set BASE_FIELDS = [CLIENT_ID,GRANT_TYPE,RESPONSE_TYPE] as Set

  static RegistrationRequest readRegistrationRequest(Form form, Headers headers) {
    GrantType grantType = GrantType.valueOf(extractField(form, GRANT_TYPE, true).toUpperCase())
    def builder = RegistrationRequest.builder()
        .grantType(grantType)
        .responseType(extractField(form, RESPONSE_TYPE, false))
        .data(extractRest(form, BASE_FIELDS))
    if (GrantType.AUTHORIZATION_CODE == grantType) {
      builder.authCode(extractField(form, CODE, true))
      def auth = extractAuth(headers)
      builder.clientId(auth[0])
          .clientSecret(auth[1])
    } else {
      builder.clientId(extractField(form, CLIENT_ID, true))
    }
    return builder.build()
  }
}