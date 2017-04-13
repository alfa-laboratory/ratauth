package ru.ratauth.server.handlers.readers

import groovy.transform.CompileStatic;
import ratpack.form.Form;
import ratpack.http.Headers;
import ru.ratauth.interaction.InvalidateSessionRequest;
import ru.ratauth.server.services.log.ActionLogger;
import ru.ratauth.server.services.log.AuthAction;

import static ru.ratauth.server.handlers.readers.RequestUtil.extractAuth;
import static ru.ratauth.server.handlers.readers.RequestUtil.extractField;

@CompileStatic
class SessionRequestReader {

  static InvalidateSessionRequest readInvalidateByRefreshTokenRequest(Form form, Headers headers) {
    InvalidateSessionRequest.InvalidateSessionRequestBuilder builder = InvalidateSessionRequest.builder()
            .refreshToken(extractField(form, 'refresh_token', true))
    def auth = extractAuth(headers)
    builder.clientId(auth[0])
    def request = builder.build()
    ActionLogger.addBaseRequestInfo(request.clientId, AuthAction.INVALIDATE_SESSION)
    request
  }
}
