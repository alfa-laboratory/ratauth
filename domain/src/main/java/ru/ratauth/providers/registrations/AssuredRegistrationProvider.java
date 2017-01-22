package ru.ratauth.providers.registrations;

import ru.ratauth.providers.registrations.dto.AssuredRegResult;
import ru.ratauth.providers.registrations.dto.RegInput;
import rx.Observable;

/**
 * Interface of registration provider that do not split simple registration and assurance level check
 * @author mgorelikov
 * @since 22/01/17
 */
public interface AssuredRegistrationProvider extends RegistrationProvider<AssuredRegResult> {
  /**
   * Register user by some identifier entities like login or other unique identifier, set of fields depends on identity provider
   * @param input input fields container
   * @return map of user userInfo provided by concrete identity provider or Observable.empty
   * @throws ru.ratauth.exception.AuthorizationException by Observable.error
   */
  @Override
  Observable<AssuredRegResult> register(RegInput input);
}
