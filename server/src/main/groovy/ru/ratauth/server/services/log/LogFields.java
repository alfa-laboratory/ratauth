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
  SESSION_ID,
  LONG_TERM;

  public String val() {
    return name().toLowerCase();
  }
}
