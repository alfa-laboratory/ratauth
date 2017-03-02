package ru.ratauth.services;

import ru.ratauth.entities.AuthClient;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.SessionClient;
import rx.Observable;

/**
 * @author mgorelikov
 * @since 02/11/15
 * 
 * Persistence layer
 * Async token service
 */
public interface ClientService {
  /**
   * Loads abstract client from entity layer
   * @param name unique client name
   * @return loaded authClient entity
   */
  Observable<AuthClient> getClient(String name);

  /**
   * Loads only clients that belongs to relyingParty
   * @param name unique client name
   * @return loaded authClient entity
   */
  Observable<RelyingParty> getRelyingParty(String name);

  /**
   * Loads only clients that belongs to sessionClients
   * @param name unique client name
   * @return loaded authClient entity
   */
  Observable<SessionClient> getSessionClient(String name);
}
