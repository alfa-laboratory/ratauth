package ru.ratauth.inmemory.ip.services;

import ru.ratauth.entities.AuthEntry;
import ru.ratauth.entities.Session;
import ru.ratauth.entities.Token;
import ru.ratauth.exception.ExpiredException;
import ru.ratauth.services.SessionService;
import rx.Observable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static ru.ratauth.exception.ExpiredException.ID.AUTH_CODE_EXPIRED;
import static ru.ratauth.utils.StringUtils.isBlank;

public class InMemorySessionService implements SessionService {

  private final List<Session> sessions;

  public InMemorySessionService() {
    this(new ArrayList<>());
  }

  public InMemorySessionService(List<Session> validCodeSessions) {
    requireNonNull(validCodeSessions);
    this.sessions = validCodeSessions;
  }

  @Override
  public Observable<Session> create(Session session) {
    requireNonNull(session, "Session should not be null");
    this.sessions.add(session);
    return Observable.just(session);
  }

  @Override
  public Observable<Session> getByValidCode(String code, Date now) {
    if (isBlank(code) || (now == null)) {
      throw new NullPointerException("Code and date should not be null");
    }
    return sessions.stream()
            .filter(session -> session.getEntries().stream()
                    .anyMatch(authEntry -> authEntry.getAuthCode().equals(code)))
            .filter(session -> session.getExpiresIn().after(now))
            .findFirst()
            .map(Observable::just)
            .orElseGet(() -> Observable.error(new ExpiredException(AUTH_CODE_EXPIRED)));
  }

  @Override
  public Observable<Session> getByValidRefreshToken(String token, Date now) {
    if (isBlank(token) || (now == null)) {
      throw new NullPointerException("Token and date should not be null");
    }
    return sessions.stream()
            .filter(session -> session.getEntries().stream()
                    .anyMatch(authEntry -> authEntry.getRefreshToken().equals(token)))
            .filter(session -> session.getExpiresIn().after(now))
            .findFirst()
            .map(Observable::just)
            .orElseGet(() -> Observable.error(new ExpiredException(AUTH_CODE_EXPIRED)));
  }

  @Override
  public Observable<Session> getByValidSessionToken(String token, Date now) {
    if (isBlank(token) || (now == null)) {
      throw new NullPointerException("Token and date should not be null");
    }

    return sessions.stream()
            .filter(session -> session.getSessionToken().equals(token))
            .filter(session -> session.getExpiresIn().after(now))
            .findFirst()
            .map(Observable::just)
            .orElseGet(() -> Observable.error(new ExpiredException(AUTH_CODE_EXPIRED)));
  }

  @Override
  public Observable<Session> getByValidToken(String token, Date now) {
    if (isBlank(token) || (now == null)) {
      throw new NullPointerException("Token and date should not be null");
    }
    return sessions.stream()
            .filter(session -> session.getEntries().stream()
                    .anyMatch(authEntry -> authEntry.getTokens().stream()
                            .anyMatch(token1 -> token1.getToken().equals(token))))
            .filter(session -> session.getExpiresIn().after(now))
            .findFirst()
            .map(Observable::just)
            .orElseGet(() -> Observable.error(new ExpiredException(AUTH_CODE_EXPIRED)));
  }

  @Override
  public Observable<Boolean> addEntry(String sessionId, AuthEntry entry) {
    if (isBlank(sessionId) || (entry == null)) {
      throw new NullPointerException(format("session id and %s should not be null", entry.getClass().getName()));
    }
    Session result = sessions.stream()
            .filter(session -> session.getId().equals(sessionId))
            .findFirst()
            .orElseThrow(() -> new ExpiredException(AUTH_CODE_EXPIRED));
    result.getEntries().add(entry);
    return Observable.just(result.getEntries().add(entry));
  }

  @Override
  public Observable<Boolean> addToken(String sessionId, String relyingParty, Token token) {
    if (isBlank(sessionId) || isBlank(relyingParty) || token == null) {
      throw new NullPointerException("session id, relying party and token should not be null");
    }
    Session result = sessions.stream().filter(session -> session.getId().equals(sessionId))
            .findFirst()
            .orElseThrow(() -> new ExpiredException(AUTH_CODE_EXPIRED));
    AuthEntry authEntry = result.getEntry(relyingParty).get();
    return Observable.just(authEntry.addToken(token));
  }

  @Override
  public Observable<Boolean> invalidateSession(String sessionId, Date blocked) {
    if (isBlank(sessionId) || (blocked == null)) {
      throw new NullPointerException("Session id and blocked date should not be null");
    }
    Session result = sessions.stream()
            .filter(session -> session.getId().equals(sessionId))
            .findFirst()
            .get();
    Date oldBlockedDate = result.getBlocked();

    result.setBlocked(blocked);

    return Observable.just(!blocked.equals(oldBlockedDate));
  }

  @Override
  public Observable<Boolean> invalidateForUser(String userId, Date blocked) {
    if (isBlank(userId) || (blocked == null)) {
      throw new NullPointerException("User id and blocked date should not be null");
    }
    Session result = sessions.stream()
            .filter(session -> session.getUserId().equals(userId))
            .findFirst()
            .get();
    Date oldBlockedDate = result.getBlocked();

    result.setBlocked(blocked);

    return Observable.just(!blocked.equals(oldBlockedDate));
  }

  @Override
  public Observable<Boolean> invalidateForClient(String relyingParty, Date blocked) {
    if (isBlank(relyingParty) || (blocked == null)) {
      throw new NullPointerException("Relying party and blocked date should not be null");
    }
    Session result = sessions.stream()
            .filter(session -> session.getEntry(relyingParty) != null)
            .findFirst()
            .get();
    Date oldBlockedDate = result.getBlocked();

    result.setBlocked(blocked);

    return Observable.just(!blocked.equals(oldBlockedDate));
  }

  @Override
  public Observable<Boolean> updateCheckDate(String sessionId, Date lastCheck) {
    Session result = sessions.stream()
            .filter(session -> session.getId().equals(sessionId))
            .findFirst()
            .get();
    Date oldLastCheck = result.getLastCheck();

    result.setLastCheck(lastCheck);

    return Observable.just(!lastCheck.equals(oldLastCheck));
  }
}
