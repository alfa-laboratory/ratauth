package ru.ratauth.server.services.log;

/**
 * @author mgorelikov
 * @since 25/03/16
 */
public enum LogFields {
  APPLICATION,
  CLIENT_ID,
  USER_ID,
  ACTION,
  REQUEST_ID,
  MONGO_SESSION_ID,
  LONG_TERM,
  RESPONSE_PAYLOAD,
  ERROR_MESSAGE;

  public String val() {
    return name().toLowerCase();
  }
}
