package ru.ratauth.server.handlers.readers

import ratpack.form.Form
import ratpack.http.Headers
import ru.ratauth.interaction.RegistrationRequest
import static ru.ratauth.server.utils.RequestUtil.*

/**
 * @author mgorelikov
 * @since 29/01/16
 */
class RegistrationRequestReader {
  private static final String CLIENT_ID = "client_id"
  private static final String RESPONSE_TYPE = "response_type"

  static RegistrationRequest readRegistrationRequest(Form form, Headers headers) {
    RegistrationRequest.builder()
        .clientId(extractField(form, CLIENT_ID, true))
        .responseType(extractField(form, RESPONSE_TYPE, false))
        .data(extractRest(form, [CLIENT_ID] as Set))
        .build()
  }
}