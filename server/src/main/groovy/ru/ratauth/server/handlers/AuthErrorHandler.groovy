package ru.ratauth.server.handlers

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.netty.handler.codec.http.HttpResponseStatus
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context
import ratpack.http.MediaType
import ru.ratauth.exception.*
import ru.ratauth.utils.ExceptionUtils

import static ru.ratauth.server.services.log.LogFields.ERROR_MESSAGE

/**
 * @author mgorelikov
 * @since 19/11/15
 */
@Slf4j
@Component
@CompileStatic
class AuthErrorHandler implements ServerErrorHandler {

    @Autowired
    ObjectMapper jacksonObjectMapper

    public static final int AUTHENTICATION_TIMEOUT = 419
    public static final int MAX_EXCEPTION_DEPTH = 10

    @Override
    void error(Context context, Throwable throwable) throws Exception {
        MDC.put(ERROR_MESSAGE.val(), throwable.message)
        def exception = ExceptionUtils.getThrowable(throwable, BaseAuthServerException, MAX_EXCEPTION_DEPTH)

        if (exception instanceof ExpiredException) {
            sendIdentifiedError(context, AUTHENTICATION_TIMEOUT, exception)
        } else if (exception instanceof ReadRequestException) {
            sendIdentifiedError context, HttpResponseStatus.BAD_REQUEST.code(), exception
        } else if (exception instanceof AuthorizationException || exception instanceof RegistrationException) {
            sendIdentifiedError context, HttpResponseStatus.FORBIDDEN.code(), (IdentifiedException) exception
        } else if (exception instanceof IdentifiedException) {
            sendIdentifiedError context, HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), exception
        } else if (exception) {
            sendError context, HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), exception
        } else {
            sendError context, HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), throwable
        }
    }

    private static void sendError(Context context, int code, Throwable throwable) {
        MDC.put(ERROR_MESSAGE.val(), throwable.message)
        context.response.status(code)
        context.response.send(String.valueOf(throwable.message))
        log.error("Auth error: ", throwable)
    }

    private void sendIdentifiedError(Context context, int code, IdentifiedException exception) {
        MDC.put(ERROR_MESSAGE.val(), exception.message)
        context.response.status(code)
        context.response.contentType(MediaType.APPLICATION_JSON)
        def dto = jacksonObjectMapper.writeValueAsString(new ExceptionDTO(exception))
        log.error(dto, exception as Throwable)
        context.response.send(MediaType.APPLICATION_JSON, dto)
    }

    static Exception castToException(ExceptionDTO e) {

        String message = e.message.get(ExceptionDTO.DEFAULT_LANG)
        if (!message) {
            throw new ProviderException(ProviderException.ID.DESERIALIZATION_ERROR, "Can't deserialize ${e}")
        }

        if (e.clazz == AuthorizationException as String) {
            return new AuthorizationException(e.id, message)
        } else if (e.clazz == RegistrationException as String) {
            return new RegistrationException(e.id, message)
        }
        return new ProviderException(e.id, message)
    }

    static class ExceptionDTO {
        @JsonProperty("base_id")
        String baseId
        @JsonProperty("type_id")
        String typeId
        String id
        Map<String, String> message
        @JsonProperty("class")
        String clazz

        private static final String DEFAULT_LANG = "en"

        ExceptionDTO() {

        }

        ExceptionDTO(IdentifiedException exception) {
            this.baseId = exception.baseId
            this.typeId = exception.typeId
            this.id = exception.id
            clazz = exception.getClass().toString()
            message = [
                    (DEFAULT_LANG): exception.message
            ] as Map
        }
    }
}
