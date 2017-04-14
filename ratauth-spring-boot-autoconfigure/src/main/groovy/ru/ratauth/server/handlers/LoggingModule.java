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
  @Value("${spring.application.name}")
  private String applicationName;

  @Override
  protected void configure() {
    bind(RequestId.Generator.class).toInstance(RequestId.Generator.randomUuid());
    bind(MDCInterceptor.class).toInstance(MDCInterceptor.instance());
    Multibinder.newSetBinder(binder(), HandlerDecorator.class).addBinding().toInstance(HandlerDecorator.prepend(ctx -> {
      MDC.put(LogFields.REQUEST_ID.val(), ctx.get(RequestId.class).toString());
      MDC.put(LogFields.APPLICATION.val(), applicationName);
      ctx.next();
    }));
  }
}
