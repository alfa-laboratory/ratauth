package ru.ratauth.server.services;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.*;
import ru.ratauth.interaction.TokenRequest;
import ru.ratauth.providers.Fields;
import ru.ratauth.providers.auth.dto.BaseAuthFields;
import ru.ratauth.server.secutiry.OAuthIssuer;
import ru.ratauth.server.secutiry.TokenProcessor;
import ru.ratauth.server.services.log.ActionLogger;
import ru.ratauth.server.utils.DateUtils;
import ru.ratauth.services.SessionService;
import rx.Observable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author djassan
 * @since 17/02/16
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdSessionService implements AuthSessionService {
  private final SessionService sessionService;
  private final TokenProcessor tokenProcessor;
  private final TokenCacheService tokenCacheService;
  private final OAuthIssuer codeGenerator;
  private final ActionLogger actionLogger;

  @Value("${auth.master_secret}")
  private String masterSecret;//final

  public static final String RATAUTH = "ratauth";

  @Override
  public Observable<Session> initSession(RelyingParty relyingParty, Map<String, Object> userInfo, Set<String> scopes, Set<String> authContext,
                                         String redirectUrl) {
    final LocalDateTime now = LocalDateTime.now();
    return createSession(relyingParty, userInfo, scopes, authContext, redirectUrl, now, null);
  }

  @Override
  public Observable<Session> createSession(RelyingParty relyingParty, Map<String, Object> userInfo, Set<String> scopes, Set<String> authContext,
                                           String redirectUrl) {
    final LocalDateTime now = LocalDateTime.now();
    final LocalDateTime tokenExpires = now.plus(relyingParty.getTokenTTL(), ChronoUnit.SECONDS);
    final Token token = Token.builder()
        .token(codeGenerator.accessToken())
        .expiresIn(DateUtils.fromLocal(tokenExpires))
        .created(DateUtils.fromLocal(now))
        .build();
    return createSession(relyingParty, userInfo, scopes, authContext, redirectUrl, now, token);
  }

  private Observable<Session> createSession(RelyingParty relyingParty, Map<String, Object> userInfo, Set<String> scopes,
                                            Set<String> authContext, String redirectUrl, LocalDateTime now, Token token) {
    final LocalDateTime sessionExpires = now.plus(relyingParty.getSessionTTL(), ChronoUnit.SECONDS);
    final LocalDateTime refreshExpires = now.plus(relyingParty.getRefreshTokenTTL(), ChronoUnit.SECONDS);
    final LocalDateTime authCodeExpires = now.plus(relyingParty.getCodeTTL(), ChronoUnit.SECONDS);


    final String jwtInfo = tokenProcessor.createToken(RATAUTH, masterSecret, null,
        DateUtils.fromLocal(now), DateUtils.fromLocal(sessionExpires),
        tokenCacheService.extractAudience(scopes), scopes, authContext, userInfo.get(Fields.USER_ID.val()).toString(), userInfo);

    final AuthEntry authEntry = AuthEntry.builder()
        .created(DateUtils.fromLocal(now))
        .authCode(codeGenerator.authorizationCode())
        .codeExpiresIn(DateUtils.fromLocal(authCodeExpires))
        .refreshToken(codeGenerator.refreshToken())
        .refreshTokenExpiresIn(DateUtils.fromLocal(refreshExpires))
        .scopes(scopes)
        .authContext(new HashSet<>())
        .relyingParty(relyingParty.getName())
        .authType(AuthType.COMMON)
        .redirectUrl(redirectUrl)
        .build();
    authEntry.addToken(token);
    final Session session = Session.builder()
        .sessionToken(codeGenerator.refreshToken())
        .identityProvider(relyingParty.getIdentityProvider())
        .authClient(relyingParty.getName())
        .status(Status.ACTIVE)
        .created(DateUtils.fromLocal(now))
        .expiresIn(DateUtils.fromLocal(sessionExpires))
        .userId((String) userInfo.get(BaseAuthFields.USER_ID.val()))
        .userInfo(jwtInfo)
        .entries(new HashSet<>(Arrays.asList(authEntry)))
        .build();
    return sessionService.create(session)
        .doOnNext(actionLogger::addSessionInfo);
  }

  @Override
  public Observable<Boolean> addToken(TokenRequest oauthRequest, Session session, RelyingParty relyingParty) {
    final LocalDateTime now = LocalDateTime.now();
    final LocalDateTime tokenExpires = now.plus(relyingParty.getTokenTTL(), ChronoUnit.SECONDS);
    final Token token = Token.builder()
        .token(codeGenerator.accessToken())
        .expiresIn(DateUtils.fromLocal(tokenExpires))
        .created(DateUtils.fromLocal(now))
        .build();
    return sessionService.addToken(session.getId(), relyingParty.getName(), token)
        .doOnNext(subs -> session.getEntry(relyingParty.getName()).ifPresent(entry -> entry.addToken(token)));
  }

  @Override
  public Observable<Session> addEntry(Session session, RelyingParty relyingParty, Set<String> scopes, String redirectUrl) {
    final LocalDateTime now = LocalDateTime.now();
    final LocalDateTime refreshExpires = now.plus(relyingParty.getRefreshTokenTTL(), ChronoUnit.SECONDS);
    final LocalDateTime authCodeExpires = now.plus(relyingParty.getCodeTTL(), ChronoUnit.SECONDS);
    final AuthEntry authEntry = AuthEntry.builder()
        .created(DateUtils.fromLocal(now))
        .authCode(codeGenerator.authorizationCode())
        .codeExpiresIn(DateUtils.fromLocal(authCodeExpires))
        .refreshToken(codeGenerator.refreshToken())
        .refreshTokenExpiresIn(DateUtils.fromLocal(refreshExpires))
        .scopes(scopes)
        .relyingParty(relyingParty.getName())
        .authType(AuthType.CROSS)
        .redirectUrl(redirectUrl)
        .build();
    return sessionService.addEntry(session.getId(), authEntry)
        .doOnNext(res -> session.getEntries().add(authEntry))
        .map(res -> session);
  }

  @Override
  public Observable<Session> getByValidCode(String code, Date now) {
    return sessionService.getByValidCode(code, now)
        .doOnNext(actionLogger::addSessionInfo);
  }

  @Override
  public Observable<Session> getByValidRefreshToken(String token, Date now) {
    return sessionService.getByValidRefreshToken(token, now)
        .doOnNext(actionLogger::addSessionInfo);
  }

  @Override
  public Observable<Session> getByValidSessionToken(String token, Date now) {
    return sessionService.getByValidSessionToken(token, now)
      .doOnNext(actionLogger::addSessionInfo);
  }

  @Override
  public Observable<Session> getByValidToken(String token, Date now) {
    return sessionService.getByValidToken(token, now)
        .doOnNext(actionLogger::addSessionInfo);
  }
}
