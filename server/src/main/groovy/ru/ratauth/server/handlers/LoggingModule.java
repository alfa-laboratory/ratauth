package ru.ratauth.server.handlers;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ratpack.handling.HandlerDecorator;
import ratpack.handling.RequestId;
import ratpack.http.Headers;
import ratpack.http.Request;
import ratpack.logging.MDCInterceptor;
import ru.ratauth.server.secutiry.TraceIdGenerator;

import static ru.ratauth.server.handlers.readers.RequestUtil.extractField;
import static ru.ratauth.server.services.log.LogFields.*;
import static ru.ratauth.server.services.log.LogHeader.X_B3_SPAN_ID;
import static ru.ratauth.server.services.log.LogHeader.X_B3_TRACE_ID;
import static ru.ratauth.utils.StringUtils.isBlank;

/**
 * @author mgorelikov
 * @since 24/03/16
 */
@Component
public class LoggingModule extends AbstractModule {
    private static final String ALTERNATIVE_DEVICE_ID = "DEVICE-ID";

    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    protected void configure() {
        bind(RequestId.Generator.class).toInstance(RequestId.Generator.header(TRACE_ID.val(), RequestId.Generator.randomUuid()));
        bind(MDCInterceptor.class).toInstance(MDCInterceptor.instance());
        Multibinder.newSetBinder(binder(), HandlerDecorator.class).addBinding().toInstance(HandlerDecorator.prepend(ctx -> {
            String requestId = ctx.get(RequestId.class).toString();
            MDC.put(TRACE_ID.val(), requestId);
            Request request = ctx.getRequest();
            String deviceId = extractField(request.getQueryParams(), DEVICE_ID.val(), false);
            Headers headers = request.getHeaders();
            if (isBlank(deviceId)) {
                deviceId = headers.get(DEVICE_ID.val());
            }
            if (isBlank(deviceId)) {
                deviceId = headers.get(ALTERNATIVE_DEVICE_ID);
            }
            if (!isBlank(deviceId)) {
                MDC.put(DEVICE_ID.val(), deviceId);
            }
            MDC.put(APPLICATION.val(), applicationName);
            String sessionId = headers.get(SESSION_ID.val());
            if (!isBlank(sessionId)) {
                MDC.put(SESSION_ID.val(), sessionId);
            }
            forwardTrace(headers);
            ctx.next();
        }));
    }

    private void forwardTrace(Headers headers) {
        if (isBlank(headers.get(X_B3_TRACE_ID.headerVal()))) {
            String initTraceId = TraceIdGenerator.generateValue();
            MDC.put(X_B3_TRACE_ID.mdcVal(), initTraceId);
            MDC.put(X_B3_SPAN_ID.mdcVal(), initTraceId);
        } else {
            MDC.put(X_B3_TRACE_ID.mdcVal(), headers.get(X_B3_TRACE_ID.headerVal()));
            MDC.put(X_B3_SPAN_ID.mdcVal(), headers.get(X_B3_SPAN_ID.headerVal()));
        }
    }
}
