package ru.ratauth.server.services.log;

/**
 * @author mgorelikov
 * @since 24/03/16
 */
public enum AuthAction {
  REGISTRATION(true),
  AUTHORIZATION(true),
  TOKEN(true),
  REFRESH_TOKEN(true),
  CROSS_AUTHORIZATION(true),
  INVALIDATE_SESSION(true),
  CHECK_TOKEN(false);

  private final boolean longTerm;

  AuthAction(boolean longTerm) {
    this.longTerm = longTerm;
  }

  public boolean isLongTerm() {
    return longTerm;
  }
}
