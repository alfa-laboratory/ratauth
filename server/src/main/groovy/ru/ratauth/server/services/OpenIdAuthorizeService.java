package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.*;
import ru.ratauth.interaction.AuthzRequest;
import ru.ratauth.interaction.AuthzResponse;
import ru.ratauth.interaction.AuthzResponseType;
import ru.ratauth.interaction.TokenType;
import ru.ratauth.providers.auth.AuthProvider;
import ru.ratauth.providers.auth.dto.AuthInput;
import ru.ratauth.providers.auth.dto.AuthResult;
import rx.Observable;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
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
                .map(ses -> new ImmutablePair<>(rpAuth.getLeft(), ses)))
        .flatMap(rpSession ->
                createIdToken(rpSession.getLeft(), rpSession.getRight())
                    .map(idToken -> new ImmutablePair<>(rpSession.getRight(), idToken))
        )
        .map(sessionToken -> buildResponse(request.getRedirectURI(), request.getClientId(), sessionToken.getLeft(), sessionToken.right));
  }

  @Override
  public Observable<AuthzResponse> crossAuthenticate(AuthzRequest request) {
    return  Observable.zip(
        clientService.loadAndAuthRelyingParty(request.getClientId(), request.getClientSecret(), true),
        clientService.loadRelyingParty(request.getExternalClientId()),
        sessionService.getByValidRefreshToken(request.getRefreshToken(), new Date()),
        (oldRP, newRP, session) -> new ImmutablePair<>(newRP, session))
        .flatMap(rpSession -> sessionService.addEntry(rpSession.getRight(), rpSession.getLeft(), request.getScopes(), request.getRedirectURI()))
        .map(session -> buildResponse(request.getRedirectURI(), request.getExternalClientId(), session, null));
  }

  private Observable<TokenCache> createIdToken(RelyingParty relyingParty, Session session) {
    Optional<AuthEntry> entry = session.getEntry(relyingParty.getName());
    Optional<Token> token = entry.flatMap(el -> el.getLatestToken());
    if (session != null && token.isPresent())
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
      return Observable.just((Session) null);//authCode provided by external Auth provider
  }

  private Observable<AuthResult> authenticateUser(Map<String, String> authData, String identityProvider, String relyingPartyName) {
    return authProviders.get(identityProvider).authenticate(
        AuthInput.builder().
            data(authData)
            .relyingParty(relyingPartyName).build());
  }

  private static AuthzResponse buildResponse(String redirectURL, String clientId, Session session, TokenCache tokenCache) {
    //in case of autCode sent by authProvider
    if (session == null) {
      return AuthzResponse.builder()
          .location(redirectURL).build();
    }

    AuthEntry entry = session.getEntry(clientId).get();
    AuthzResponse resp = AuthzResponse.builder()
        .location(entry.getRedirectUrl()).build();
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
}
