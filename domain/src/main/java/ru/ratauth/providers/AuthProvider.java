package ru.ratauth.providers;

import rx.Observable;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 01/11/15
 * interface for authentication providers
 */
public interface AuthProvider {
  /**
   * constants for standard user info
   */
  public final String USER_ID="user_id";
  /**
   *
   * @param login
   * @param password
   * @return map of user data provided by concrete identity provider
   */
  Observable<Map<String, Object>> checkCredentials(String login, String password);
}
