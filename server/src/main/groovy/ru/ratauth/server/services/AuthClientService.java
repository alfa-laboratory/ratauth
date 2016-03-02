package ru.ratauth.server.services;

import ru.ratauth.entities.AuthClient;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.exception.AuthorizationException;
import rx.Observable;

/**
 * @author mgorelikov
 * @since 16/02/16
 */
public interface AuthClientService {

  /**
   * Just loads relyingParty by name
   *
   * @param name unique name
   * @return observable of loaded AuthClient
   */
  Observable<RelyingParty> loadRelyingParty(String name);

  /**
   * Just loads client by name
   *
   * @param name unique name
   * @return observable of loaded AuthClient
   */
  Observable<AuthClient> loadClient(String name);

  /**
   * Loads Relying party by name and checks it password in case of auth required
   *
   * @param name         unique name
   * @param password     relying party password
   * @param authRequired flag that auth required
   * @return observable of loaded relyingParty
   */
  default Observable<RelyingParty> loadAndAuthRelyingParty(String name, String password, boolean authRequired) {
    Observable<RelyingParty> relyingPartyObservable = loadRelyingParty(name);
    return addAuth(relyingPartyObservable, password, authRequired);
  }

  /**
   * Loads AuthClient by name and checks it password in case of auth required
   *
   * @param name         unique name
   * @param password     AuthClient password
   * @param authRequired flag that auth required
   * @return observable of loaded authClient
   */
  default Observable<AuthClient> loadAndAuthClient(String name, String password, boolean authRequired) {
    Observable<AuthClient> authClientObservable = loadClient(name);
    return addAuth(authClientObservable, password, authRequired);
  }

  default <T extends AuthClient> Observable<T> addAuth(Observable<T> clientObservable, String password, boolean authRequired) {
    return clientObservable.filter(rp -> !authRequired || rp.getPassword().equals(password))
        .switchIfEmpty(Observable.error(new AuthorizationException("Client not found")));
  }

}
