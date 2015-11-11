package ru.ratauth.services;

import ru.ratauth.entities.AuthzEntry;
import rx.Observable;

import java.util.Date;

/**
 * @author mgorelikov
 * @since 02/11/15
 *
 * Persistence layer
 * Async token service
 */
public interface AuthzEntryService {
  Observable<AuthzEntry> save(AuthzEntry code);

  /**
   * Loads AuthzEntry by code with expiration date check
   * @param code code value
   * @param now current date
   * @return Observable of single AuthzEntry or Observable.empty()
   */
  Observable<AuthzEntry> getByValidCode(String code, Date now);

  /**
   * Loads AuthzEntry by refreshToken with expiration date check
   * @param token refreshToken value
   * @param now current date
   * @return Observable of single AuthzEntry or Observable.empty()
   */
  Observable<AuthzEntry> getByValidRefreshToken(String token, Date now);

  /**
   * Loads AuthzEntry by refreshToken with expiration date check. AuthzEntry must be loaded with only one token entity
   * @param token access token value
   * @param now current date
   * @return Observable of single AuthzEntry or Observable.empty()
   */
  Observable<AuthzEntry> getByValidToken(String token, Date now);
}
