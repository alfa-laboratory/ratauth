package ru.ratauth.server.services;

import ru.ratauth.interaction.AuthzRequest;
import ru.ratauth.interaction.AuthzResponse;
import rx.Observable;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
public interface AuthorizeService {
  /**
   * Authenticate  user
   * @param request auth request
   */
  Observable<AuthzResponse> authenticate(AuthzRequest request);
  Observable<AuthzResponse> crossAuthenticate(AuthzRequest request);
}