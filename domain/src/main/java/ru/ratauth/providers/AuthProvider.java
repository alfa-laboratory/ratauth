package ru.ratauth.providers;

/**
 * @author mgorelikov
 * @since 01/11/15
 * interface for authentication providers
 */
public interface AuthProvider {
  /**
   *
   * @param login
   * @param password
   * @return user identifier in case user was found otherwise returns null
   */
  String checkCredentials(String login, String password);
}
