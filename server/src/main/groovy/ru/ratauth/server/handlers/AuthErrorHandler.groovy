package ru.ratauth.server.handlers

import groovy.util.logging.Slf4j
import io.netty.handler.codec.http.HttpResponseStatus
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context
import ru.ratauth.exception.ExpiredException
import ru.ratauth.server.handlers.readers.ReadRequestException
import ru.ratauth.exception.AuthorizationException

/**
 * @author mgorelikov
 * @since 19/11/15
 */
@Slf4j
class AuthErrorHandler implements ServerErrorHandler {
  public static final int AUTHENTICATION_TIMEOUT = 419

  @Override
  void error(Context context, Throwable throwable) throws Exception {
    boolean finished = false

    if (throwable in ExpiredException.class) {
      context.clientError(AUTHENTICATION_TIMEOUT)
      finished = true
    } else if (throwable in ReadRequestException.class) {
      context.clientError(HttpResponseStatus.BAD_REQUEST.code())
      finished = true
    } else if (throwable in AuthorizationException.class) {
      context.clientError(HttpResponseStatus.FORBIDDEN.code())
      finished = true
    } else if (throwable.cause)
      this.error(context, throwable.cause) && (finished = true)

    if (finished) {
      log.error("Auth error: " + throwable.getMessage())
      log.debug("Error stacktrace:", throwable)
    }
  }
}
