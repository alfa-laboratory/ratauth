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

/**
 * @author mgorelikov
 * @since 24/03/16
 */
@Component
public class LoggingModule extends AbstractModule {
  public static final String PERFORATING_TRACE_ID = "X-B3-TraceId";

  @Value("${spring.application.name}")
  private String applicationName;

  @Override
  protected void configure() {
    bind(RequestId.Generator.class).toInstance(RequestId.Generator.header(PERFORATING_TRACE_ID, RequestId.Generator.randomUuid()));
    bind(MDCInterceptor.class).toInstance(MDCInterceptor.instance());
    Multibinder.newSetBinder(binder(), HandlerDecorator.class).addBinding().toInstance(HandlerDecorator.prepend(ctx -> {
      String requestId = ctx.get(RequestId.class).toString();
      MDC.put(LogFields.REQUEST_ID.val(), requestId);
      MDC.put(PERFORATING_TRACE_ID, requestId);
      MDC.put(LogFields.APPLICATION.val(), applicationName);
      ctx.next();
    }));
  }
}
