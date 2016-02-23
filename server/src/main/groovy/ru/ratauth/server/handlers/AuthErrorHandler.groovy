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
      context.clientError(AUTHENTICATION_TIMEOUT)
    else if (exception in ReadRequestException.class)
      context.clientError(HttpResponseStatus.BAD_REQUEST.code())
    else if (exception in AuthorizationException.class)
      context.clientError(HttpResponseStatus.FORBIDDEN.code())


    log.error("Auth error: " + throwable.getMessage())
    log.debug("Error stacktrace:", throwable)
  }
}
