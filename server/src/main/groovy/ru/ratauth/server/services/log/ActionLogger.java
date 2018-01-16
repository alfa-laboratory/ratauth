package ru.ratauth.server.services.log;

import org.slf4j.MDC;
import ru.ratauth.entities.Session;

/**
 * @author mgorelikov
 * @since 24/03/16
 */
public interface ActionLogger {
  default void addSessionInfo(Session session) {
    MDC.put(LogFields.SESSION_ENTITY_ID.val(), session.getId());
    MDC.put(LogFields.USER_ID.val(), session.getUserId());
  }

  static void addBaseRequestInfo(String clientId, AuthAction authAction) {
      MDC.put(LogFields.ACTION.val(), authAction.name());
      MDC.put(LogFields.CLIENT_ID.val(), clientId);
      if(authAction.isLongTerm()) {
        MDC.put(LogFields.LONG_TERM.val(), Boolean.TRUE.toString());
      }
  }
}
