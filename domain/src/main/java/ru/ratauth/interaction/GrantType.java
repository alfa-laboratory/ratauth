package ru.ratauth.interaction;

/**
 * @author mgorelikov
 * @since 11/11/15
 */
public enum GrantType {
  AUTHORIZATION_CODE,
  REFRESH_TOKEN,
  /**
   * Mix of auth_code and refresh_token request.
   * Could be used in case of client needs separate token to separate aud,
   * but doesn't want to pass repeatedly through all authentication process.
   * So refresh token can be used as authentication.
   */
  AUTHENTICATION_TOKEN,
  PASSWORD
}
