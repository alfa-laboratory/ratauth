package ru.ratauth.services;

import ru.ratauth.entities.RelyingParty;
import rx.Observable;

/**
 * @author mgorelikov
 * @since 02/11/15
 * 
 * Persistence layer
 * Async token service
 */
public interface RelyingPartyService {
  /**
   * Loads relying party from entity layer
   * @param id entity identifier
   * @return loaded relying party entity
   */
  Observable<RelyingParty> getRelyingParty(String id);
}
