package ru.ratauth.server.handlers;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ratpack.handling.HandlerDecorator;
import ratpack.handling.RequestId;
import ratpack.logging.MDCInterceptor;
import ru.ratauth.server.services.log.LogFields;

import static ru.ratauth.server.handlers.readers.RequestUtil.extractField;
import static ru.ratauth.utils.StringUtils.isBlank;

/**
 * @author mgorelikov
 * @since 24/03/16
 */
@Component
public class LoggingModule extends AbstractModule {
    private static final String PERFORATING_TRACE_ID = "trace_id";
    private static final String DEVICE_ID = "device_id";

    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    protected void configure() {
        bind(RequestId.Generator.class).toInstance(RequestId.Generator.header(PERFORATING_TRACE_ID, RequestId.Generator.randomUuid()));
        bind(MDCInterceptor.class).toInstance(MDCInterceptor.instance());
        Multibinder.newSetBinder(binder(), HandlerDecorator.class).addBinding().toInstance(HandlerDecorator.prepend(ctx -> {
            String requestId = ctx.get(RequestId.class).toString();
            MDC.put(PERFORATING_TRACE_ID, requestId);
            String deviceId = extractField(ctx.getRequest().getQueryParams(), DEVICE_ID, false);
            if (!isBlank(deviceId)) {
                MDC.put(DEVICE_ID, deviceId);
            }
            MDC.put(LogFields.APPLICATION.val(), applicationName);
            ctx.next();
        }));
    }
}
