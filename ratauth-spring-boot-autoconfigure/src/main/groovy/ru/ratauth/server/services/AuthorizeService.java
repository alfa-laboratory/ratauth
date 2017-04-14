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
   * Authenticates user and creates session.
   * Creates authentication code or token in case of implicit flow
   * @param request authentication request object
   */
  Observable<AuthzResponse> authenticate(AuthzRequest request);

  /**
   * Authenticates user for relyingParty B by refresh token granted to relyingParty A
   * @param request authentication request
   */
  Observable<AuthzResponse> crossAuthenticate(AuthzRequest request);
}