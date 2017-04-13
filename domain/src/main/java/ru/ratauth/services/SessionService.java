package ru.ratauth.services;

import ru.ratauth.entities.AuthEntry;
import ru.ratauth.entities.Session;
import ru.ratauth.entities.Token;
import rx.Observable;

import java.util.Date;

/**
 * @author mgorelikov
 * @since 02/11/15
 *
 * Persistence layer
 * Async token service
 */
public interface SessionService {
  /**
   * Saves new session
   * @param session
   * @return saved entity
   */
  Observable<Session> create(Session session);

  /**
   * Loads AuthEntry by code with expiration date check. Session must be loaded with only one entry
   * @param code code value
   * @param now current date
   * @return Observable of single AuthEntry or Observable.empty if code not found or Observable.error if code has expired
   */
  Observable<Session> getByValidCode(String code, Date now);

  /**
   * Loads AuthEntry by refreshToken with expiration date check. Session must be loaded with only one entry
   * @param token refreshToken value
   * @param now current date
   * @return Observable of single AuthEntry or Observable.empty if refresh token not found or Observable.error if refresh token has expired
   */
  Observable<Session> getByValidRefreshToken(String token, Date now);

  /**
   * Loads AuthEntry by sessionToken with expiration date check. Session must be loaded with only one entry
   * @param token sessionToken value
   * @param now current date
   * @return Observable of single AuthEntry or Observable.empty if session token not found or Observable.error if session token has expired
   */
  Observable<Session> getByValidSessionToken(String token, Date now);

  /**
   * Loads Session by refreshToken with expiration date check. Session must be loaded with only one entry and token entity
   * @param token access token value
   * @param now current date
   * @return Observable of single AuthEntry with single token or Observable.empty if token not found or Observable.error if token has expired
   */
  Observable<Session> getByValidToken(String token, Date now);

  /**
   * Adds new entry into existing session
   * @param sessionId session identifier
   * @param entry new entry
   */
  Observable<Boolean> addEntry(String sessionId, AuthEntry entry);

  /**
   * Adds new entry into existing session
   * @param sessionId session identifier
   * @param relyingParty relying party unique name to find corresponding entry within session
   * @param token new token
   */
  Observable<Boolean> addToken(String sessionId, String relyingParty, Token token);

  /**
   * Invalidates session and all token database cache(if supported)
   * @param sessionId session identifier
   * @param blocked date of blocking
   */
  Observable<Boolean> invalidateSession(String sessionId, Date blocked);

  /**
   * Invalidates session and all token database cache(if supported)
   * @param userId userIdentifier
   * @param blocked date of blocking
   */
  Observable<Boolean> invalidateForUser(String userId, Date blocked);

  /**
   * Invalidates session and all token database cache(if supported)
   * @param relyingParty client unique name
   * @param blocked date of blocking
   */
  Observable<Boolean> invalidateForClient(String relyingParty, Date blocked);

//  todo ищем аксесс токен в AuthEntry.tokens или смотри как это уже реализовано в
// todo see also getByValidToken
//  OpenIdTokenService  в jmba создать свой интерфейс, создать его имплементацию
//  refreshtokenRequest openIdManager
//        EncodedField CustomUrlEncoded
//                openIdService  pull request на Андрей Трофимов
  Observable<Boolean> invalidateByRefreshToken(String relyingParty, String refreshToken);
  /**
   * Updates session lastCheck date
   * @param sessionId session identifier
   * @param lastCheck date of lastCheck
   */
  Observable<Boolean> updateCheckDate(String sessionId, Date lastCheck);
}
