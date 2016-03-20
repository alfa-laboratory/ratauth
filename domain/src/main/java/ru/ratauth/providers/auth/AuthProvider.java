package ru.ratauth.providers.auth;

import ru.ratauth.providers.auth.dto.AuthInput;
import ru.ratauth.providers.auth.dto.AuthResult;
import rx.Observable;

/**
 * @author mgorelikov
 * @since 01/11/15
 * interface for authentication providers
 */
public interface AuthProvider {
  /**
   * Authenticate user by some credentials
   * @param input input fields container
   * @return map of user data provided by concrete identity provider or Observable.empty
   * @throws ru.ratauth.exception.AuthorizationException by Observable.error
   */
  Observable<AuthResult> authenticate(AuthInput input);

  /**
   * Must return true in case of authProvider uses it's own storage of auth code and supports two phase authorization code flow.
   * Otherwise RatAuth provides authorization code flow by itself
   */
  boolean isAuthCodeSupported();

  /**
   * Checks user status
   * @param input fields container
   * @return true if user account is active, otherwise - false
   */
  Observable<Boolean> checkUserStatus(AuthInput input);
}
