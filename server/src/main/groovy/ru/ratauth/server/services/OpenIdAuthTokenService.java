package ru.ratauth.server.services;

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.ratauth.entities.*;
import ru.ratauth.entities.Status;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.exception.ExpiredException;
import ru.ratauth.interaction.*;
import ru.ratauth.interaction.TokenType;
import ru.ratauth.providers.auth.AuthProvider;
import ru.ratauth.providers.auth.dto.AuthInput;
import ru.ratauth.server.secutiry.OAuthSystemException;
import rx.Observable;

import java.util.Date;
import java.util.Map;

/**
 * @author mgorelikov
 * @since 03/11/15
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdAuthTokenService implements AuthTokenService {
  private final Map<String, AuthProvider> authProviders;
  private final AuthSessionService authSessionService;
  private final TokenCacheService tokenCacheService;
  private final AuthClientService clientService;
  private final SessionStatusChecker sessionStatusChecker;

  @Override
  @SneakyThrows
  public Observable<TokenResponse> getToken(TokenRequest oauthRequest) throws OAuthSystemException, JOSEException {
    final Observable<RelyingParty> relyingPartyObservable = clientService.loadAndAuthRelyingParty(oauthRequest.getClientId(), oauthRequest.getClientSecret(), true);
    return relyingPartyObservable
        .flatMap(rp -> loadSession(oauthRequest, rp).map(ses -> new ImmutablePair<>(rp, ses)))
        .flatMap(rpSess -> authSessionService.addToken(rpSess.getRight(), rpSess.getLeft()).map(res -> rpSess))
        .flatMap(rpSess -> createIdTokenAndResponse(rpSess.getRight(), rpSess.getLeft()))
        .doOnCompleted(() -> log.info("Get-token succeed"));
  }

  @Override
  public Observable<TokenResponse> createIdTokenAndResponse(Session session, RelyingParty relyingParty) {
    AuthEntry entry = session.getEntry(relyingParty.getName()).get();
    return tokenCacheService.getToken(session, relyingParty, entry)
        .map(idToken -> new ImmutablePair<>(entry, idToken))
        .map(entryToken -> convertToResponse(entryToken.getLeft(), entryToken.getRight().getIdToken()))
        .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.TOKEN_NOT_FOUND)));
  }

  @Override
  public Observable<CheckTokenResponse> checkToken(CheckTokenRequest oauthRequest) {
    return authSessionService.getByValidToken(oauthRequest.getToken(), new Date())
        .doOnNext(session -> sessionStatusChecker.checkAndUpdateSession(session))
        .doOnNext(session -> checkSession(session))
        .zipWith(loadRelyingParty(oauthRequest),
            (session, client) -> new ImmutablePair<>(session, client))
        .flatMap(sessionClient -> {
          //load idToken(jwt) from cache or create new
          AuthEntry entry = sessionClient.getLeft().getEntries().iterator().next();
          return tokenCacheService.getToken(sessionClient.getLeft(), sessionClient.getRight(), entry)
              .map(token -> new ImmutablePair<>(entry, token));
        })
        .map(entryToken -> {
          AuthEntry entry = entryToken.getLeft();
          Token token = entry.getTokens().iterator().next();
          return CheckTokenResponse.builder()
              .idToken(entryToken.getRight().getIdToken())
              .clientId(entryToken.getRight().getClient())
              .expiresIn(token.getExpiresIn().getTime())
              .scopes(entry.getScopes())
              .build();})
        .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.TOKEN_NOT_FOUND)))
        .doOnCompleted(() -> log.info("Check token succeed"));
  }

  private void checkSession(Session session) {
    if(Status.BLOCKED == session.getStatus())
      throw new AuthorizationException(AuthorizationException.ID.SESSION_BLOCKED);
    if(session.getExpiresIn().before(new Date()))
      throw new ExpiredException(ExpiredException.ID.SESSION_EXPIRED);
  }

  private TokenResponse convertToResponse(AuthEntry authEntry, String idToken) {
    final Token token = authEntry.getLatestToken().get();
    return TokenResponse.builder()
        .accessToken(token.getToken())
        .expiresIn(token.getExpiresIn().getTime())
        .tokenType(TokenType.BEARER.toString())
        .idToken(idToken)
        .refreshToken(authEntry.getRefreshToken())
        .clientId(authEntry.getRelyingParty())
        .build();
  }

  private Observable<Session> loadSession(TokenRequest oauthRequest, RelyingParty relyingParty) {
    Observable<Session> authObs;
    AuthProvider provider = authProviders.get(relyingParty.getIdentityProvider());
    if (provider.isAuthCodeSupported() && oauthRequest.getGrantType() == GrantType.AUTHORIZATION_CODE) {
      authObs = provider.authenticate(AuthInput.builder().relyingParty(relyingParty.getName()).data(oauthRequest.getAuthData()).build())
          .flatMap(res -> authSessionService.createSession(relyingParty, res.getData(), oauthRequest.getScopes(), null));
    } else if (oauthRequest.getGrantType() == GrantType.AUTHORIZATION_CODE)
      authObs = authSessionService.getByValidCode(oauthRequest.getAuthzCode(), new Date())
          //check that session belongs to target relying party and contains no tokens
          .filter(sess -> sess.getEntry(relyingParty.getName()).map(entry -> CollectionUtils.isEmpty(entry.getTokens())).orElse(false));
    else if (oauthRequest.getGrantType() == GrantType.REFRESH_TOKEN || oauthRequest.getGrantType() == GrantType.AUTHENTICATION_TOKEN)
      authObs = authSessionService.getByValidRefreshToken(oauthRequest.getRefreshToken(), new Date());
    else return Observable.error(new AuthorizationException(AuthorizationException.ID.INVALID_GRANT_TYPE));
    return authObs.switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.SESSION_NOT_FOUND)));
  }

  /**
   * Loads and authenticate requester(authClient) and authClient identified by externalClientId
   *
   * @param request input checkTokenRequest
   * @return requester or externalClientId in case it is defined in request
   */
  private Observable<AuthClient> loadRelyingParty(CheckTokenRequest request) {
    Observable<AuthClient> res = clientService.loadAndAuthClient(request.getClientId(), request.getClientSecret(), true);
    if (!StringUtils.isEmpty(request.getExternalClientId()))
      //since we want only to authenticate requester
      return res.zipWith(clientService.loadClient(request.getExternalClientId()),
          (client, externalClient) -> externalClient);
    else
      return res;
  }
}
