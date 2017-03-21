package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.ratauth.entities.*;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.interaction.*;
import ru.ratauth.interaction.TokenType;
import ru.ratauth.providers.auth.AuthProvider;
import ru.ratauth.providers.auth.dto.AuthInput;
import ru.ratauth.providers.auth.dto.AuthResult;
import ru.ratauth.utils.StringUtils;
import ru.ratauth.utils.URIUtils;
import rx.Observable;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdAuthorizeService implements AuthorizeService {
  private final AuthClientService clientService;
  private final TokenCacheService tokenCacheService;
  private final AuthSessionService sessionService;
  private final Map<String, AuthProvider> authProviders;

  @Override
  @SneakyThrows
  public Observable<AuthzResponse> authenticate(AuthzRequest request) {
    return clientService.loadAndAuthRelyingParty(request.getClientId(), request.getClientSecret(),
        request.getResponseType() != AuthzResponseType.CODE)
        .flatMap(rp ->
            authenticateUser(request.getAuthData(), rp.getIdentityProvider(), rp.getName())
                .map(authRes -> new ImmutablePair<>(rp, authRes)))
        .flatMap(rpAuth ->
            createSession(request, rpAuth.getRight(), rpAuth.getLeft())
                .map(ses -> new ImmutableTriple<>(rpAuth.getLeft(), rpAuth.getRight(), ses)))
        .flatMap(rpAuthSession ->
            createIdToken(rpAuthSession.getLeft(), rpAuthSession.getRight())
                .map(idToken -> buildResponse(rpAuthSession.getLeft(), rpAuthSession.getRight(),
                    rpAuthSession.getMiddle(), idToken, request.getRedirectURI()))
        )
        .doOnCompleted(() -> log.info("Authorization succeed"));
  }

  //TODO check token relation
  @Override
  public Observable<AuthzResponse> crossAuthenticate(AuthzRequest request) {
    Observable<Session> sessionObs;
    Observable<? extends AuthClient> authClientObs;
    if(GrantType.AUTHENTICATION_TOKEN == request.getGrantType()) {
      sessionObs = sessionService.getByValidRefreshToken(request.getRefreshToken(), new Date());
      authClientObs = clientService.loadAndAuthRelyingParty(request.getClientId(), request.getClientSecret(), true);
    } else {
      sessionObs = sessionService.getByValidSessionToken(request.getSessionToken(), new Date());
      authClientObs = clientService.loadAndAuthSessionClient(request.getClientId(), request.getClientSecret(), true);
    }

    return Observable.zip(
        authClientObs
            .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.CREDENTIALS_WRONG))),
        clientService.loadRelyingParty(request.getExternalClientId())
            .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.CLIENT_NOT_FOUND))),
        sessionObs
            .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.TOKEN_NOT_FOUND))),
        (oldRP, newRP, session) -> new ImmutablePair<>(newRP, session)
    ).flatMap(rpSession -> {
      String redirectURI = createRedirectURI(rpSession.getLeft(), request.getRedirectURI());
      return sessionService.addEntry(rpSession.getRight(), rpSession.getLeft(), request.getScopes(), redirectURI)
            .map(session -> buildResponse(rpSession.getLeft(), session,
                AuthResult.builder().status(AuthResult.Status.NEED_APPROVAL).build(), null, request.getRedirectURI()));
      }
    ).doOnCompleted(() -> log.info("Cross-authorization succeed"));
  }

  private Observable<TokenCache> createIdToken(RelyingParty relyingParty, Session session) {
    Optional<AuthEntry> entry = session.getEntry(relyingParty.getName());
    Optional<Token> token = entry.flatMap(el -> el.getLatestToken());
    if (token.isPresent())
      return tokenCacheService.getToken(session, relyingParty, entry.get());
    else
      return Observable.just((TokenCache) null);
  }

  private Observable<Session> createSession(AuthzRequest oauthRequest, AuthResult authResult, RelyingParty relyingParty) {
    if (AuthResult.Status.SUCCESS == authResult.getStatus())
      if (AuthzResponseType.TOKEN == oauthRequest.getResponseType())//implicit auth
        return sessionService.createSession(relyingParty, authResult.getData(), oauthRequest.getScopes(), oauthRequest.getRedirectURI());
      else//auth code auth
        return sessionService.initSession(relyingParty, authResult.getData(), oauthRequest.getScopes(), oauthRequest.getRedirectURI());
    else
      return Observable.just(new Session());//authCode provided by external Auth provider
  }

  private Observable<AuthResult> authenticateUser(Map<String, String> authData, String identityProvider, String relyingPartyName) {
    return authProviders.get(identityProvider).authenticate(
        AuthInput.builder().
            data(authData)
            .relyingParty(relyingPartyName).build());
  }

  private static AuthzResponse buildResponse(RelyingParty relyingParty, Session session,AuthResult authResult, TokenCache tokenCache, String redirectUri) {
    final String targetRedirectURI = createRedirectURI(relyingParty, redirectUri);
    //in case of autCode sent by authProvider
    if (session == null || CollectionUtils.isEmpty(session.getEntries())) {
      return AuthzResponse.builder()
          .location(targetRedirectURI)
          .data(authResult.getData())
          .build();
    }

    AuthEntry entry = session.getEntry(relyingParty.getName()).get();
    AuthzResponse resp = AuthzResponse.builder()
        .location(entry.getRedirectUrl())
        .data(authResult.getData())
        .build();
    final Optional<Token> tokenOptional = entry.getLatestToken();
    //implicit auth
    if (tokenOptional.isPresent()) {
      final Token token = tokenOptional.get();
      resp.setToken(token.getToken());
      resp.setIdToken(tokenCache.getIdToken());
      resp.setTokenType(TokenType.BEARER);
      resp.setRefreshToken(entry.getRefreshToken());
      resp.setExpiresIn(token.getExpiresIn().getTime());
    } else {//auth code authorization
      resp.setCode(entry.getAuthCode());
      resp.setExpiresIn(entry.getCodeExpiresIn().getTime());
    }
    return resp;
  }

  private static String createRedirectURI(RelyingParty relyingParty, String redirectUri) {
    if(StringUtils.isBlank(redirectUri)) {
      return relyingParty.getAuthorizationRedirectURI();
    } else {
      if(!URIUtils.compareHosts(redirectUri, relyingParty.getRedirectURIs()))
        throw new AuthorizationException(AuthorizationException.ID.REDIRECT_NOT_CORRECT);
      else
        return redirectUri;
    }
  }
}
