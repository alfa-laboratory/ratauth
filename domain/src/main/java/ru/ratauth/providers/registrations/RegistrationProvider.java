package ru.ratauth.providers.registrations;

import ru.ratauth.providers.registrations.dto.RegInput;
import ru.ratauth.providers.registrations.dto.RegResult;
import rx.Observable;

/**
 * @author mgorelikov
 * @since 28/01/16
 */
public interface RegistrationProvider {
  /**
   * Register user by some identifier entities like login or other unique identifier, set of fields depends on identity provider
   * @param input input fields container
   * @return map of user data provided by concrete identity provider or Observable.empty
   * @throws ru.ratauth.exception.AuthorizationException by Observable.error
   */
  public Observable<RegResult> register(RegInput input);

  /**
   * Must return true in case of authProvider uses it's own storage of auth code and supports two phase registration code flow.
   * Otherwise RatAuth provides authorization code flow by itself
   */
  boolean isRegCodeSupported();
}
