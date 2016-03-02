package ru.ratauth.server.handlers

import groovy.util.logging.Slf4j
import io.netty.handler.codec.http.HttpResponseStatus
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.exception.BaseAuthServerException
import ru.ratauth.exception.ExpiredException
import ru.ratauth.server.handlers.readers.ReadRequestException
import ru.ratauth.server.utils.ExceptionUtils

/**
 * @author mgorelikov
 * @since 19/11/15
 */
@Slf4j
class AuthErrorHandler implements ServerErrorHandler {
  public static final int AUTHENTICATION_TIMEOUT = 419
  public static final int MAX_EXCEPTION_DEPTH = 10

  @Override
  void error(Context context, Throwable throwable) throws Exception {
    def exception = ExceptionUtils.getThrowable(throwable, BaseAuthServerException.class, MAX_EXCEPTION_DEPTH)

    if (exception in ExpiredException.class)
      sendError(context, AUTHENTICATION_TIMEOUT, exception.getMessage())
    else if (exception in ReadRequestException.class)
      sendError(context, HttpResponseStatus.BAD_REQUEST.code(), exception.getMessage())
    else if (exception in AuthorizationException.class)
      sendError(context, HttpResponseStatus.FORBIDDEN.code(), exception.getMessage())
    else
      context.error(exception)

    log.error("Auth error: " + throwable.getMessage())
    log.debug("Error stacktrace:", throwable)
  }

  private static void sendError(Context context, int code, String body) {
    context.response.status(code)
    context.response.send(body)
  }
}
