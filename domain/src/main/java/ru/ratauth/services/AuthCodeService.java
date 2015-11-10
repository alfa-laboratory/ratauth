package ru.ratauth.services;

import ru.ratauth.entities.AuthCode;
import rx.Observable;

/**
 * @author mgorelikov
 * @since 02/11/15
 *
 * Persistence layer
 * Async token service
 */
public interface AuthCodeService {
  Observable<AuthCode> save(AuthCode code);
  /**
   * Loads AuthCode from entity layer by code value
   * @param code entity identifier
   * @return code entity if it was found, otherwise must return null
   */
  Observable<AuthCode> get(String code);
}
