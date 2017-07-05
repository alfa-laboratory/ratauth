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
import ru.ratauth.providers.auth.Verifier;
import ru.ratauth.providers.auth.dto.VerifyInput;
import ru.ratauth.providers.auth.dto.VerifyResult;
import ru.ratauth.server.providers.IdentityProviderResolver;
import ru.ratauth.server.providers.VerifierResolver;
import rx.Observable;

import java.util.*;

import static ru.ratauth.providers.auth.dto.VerifyResult.Status.NEED_APPROVAL;
import static ru.ratauth.server.utils.RedirectUtils.createRedirectURI;

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
  private final IdentityProviderResolver identityProviderResolver;

  @Override
  @SneakyThrows
  public Observable<AuthzResponse> authenticate(AuthzRequest request) {
    return clientService.loadAndAuthRelyingParty(request.getClientId(), request.getClientSecret(),
        request.getResponseType() != AuthzResponseType.CODE)
        .flatMap(rp ->
            authenticateUser(request.getAuthData(), request.getAcrValues(), rp.getIdentityProvider(), rp.getName())
                .map(authRes -> new ImmutableTriple<>(rp, authRes, request.getAcrValues())))
        .flatMap(rpAuth ->
            createSession(request, rpAuth.getMiddle(), rpAuth.getRight(), rpAuth.getLeft())
                .flatMap(ses -> createIdToken(rpAuth.left, ses, rpAuth.right)
                                .map(idToken -> buildResponse(rpAuth.left, ses, rpAuth.middle, idToken, request.getRedirectURI()))))
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
      String redirectURI = rpSession.getLeft().getAuthorizationRedirectURI();
      return sessionService.addEntry(rpSession.getRight(), rpSession.getLeft(), request.getScopes(), redirectURI)
            .map(session -> buildResponse(rpSession.getLeft(), session,
                new VerifyResult(Collections.emptyMap(), NEED_APPROVAL), null, request.getRedirectURI()));
      }
    ).doOnCompleted(() -> log.info("Cross-authorization succeed"));
  }

  private Observable<TokenCache> createIdToken(RelyingParty relyingParty, Session session, AcrValues acrValues) {
    Optional<AuthEntry> entry = session.getEntry(relyingParty.getName());
    Optional<Token> token = entry.flatMap(AuthEntry::getLatestToken);
    if (token.isPresent()){
      assert entry.isPresent();
      AuthEntry authEntry = entry.get();
      authEntry.mergeAuthContext(acrValues.getAcrValues());
      return tokenCacheService.getToken(session, relyingParty, entry.get());
    }
    else
      return Observable.just((TokenCache) null);
  }

  private Observable<Session> createSession(AuthzRequest oauthRequest, VerifyResult verifyResult, AcrValues acrValues, RelyingParty relyingParty) {
    if (VerifyResult.Status.SUCCESS == verifyResult.getStatus()) {
        if (AuthzResponseType.TOKEN == oauthRequest.getResponseType()) {//implicit auth
            return sessionService.createSession(relyingParty, verifyResult.getData(), oauthRequest.getScopes(), acrValues, oauthRequest.getRedirectURI());
        } else {//auth code auth
            return sessionService.initSession(relyingParty, verifyResult.getData(), oauthRequest.getScopes(), acrValues, oauthRequest.getRedirectURI());
        }
    } else{
      return Observable.just(new Session());//authCode provided by external Auth provider
    }
  }

  private Observable<VerifyResult> authenticateUser(Map<String, String> authData, AcrValues enroll, String identityProviderName, String relyingPartyName) {
      IdentityProvider provider = identityProviderResolver.getProvider(identityProviderName);
      VerifyInput verifyInput = new VerifyInput(authData, enroll, new UserInfo(), relyingPartyName);
      return provider.verify(verifyInput);
  }

  private static AuthzResponse buildResponse(RelyingParty relyingParty, Session session, VerifyResult verifyResult, TokenCache tokenCache, String redirectUri) {
    final String targetRedirectURI = createRedirectURI(relyingParty, redirectUri);
    //in case of autCode sent by authProvider
    if (session == null || CollectionUtils.isEmpty(session.getEntries())) {
      AuthzResponse resp = AuthzResponse.builder()
          .location(relyingParty.getAuthorizationRedirectURI())
          .data(verifyResult.getData())
          .redirectURI(targetRedirectURI)
          .build();
      return resp;
    }

    AuthEntry entry = session.getEntry(relyingParty.getName()).get();
    AuthzResponse resp = AuthzResponse.builder()
        .location(entry.getRedirectUrl())
        .data(verifyResult.getData())
        .build();
    final Optional<Token> tokenOptional = entry.getLatestToken();
    //implicit auth
    if (tokenOptional.isPresent()) {
      final Token token = tokenOptional.get();
      resp.setToken(token.getToken());
      if(tokenCache != null)
        resp.setIdToken(tokenCache.getIdToken());
      resp.setTokenType(TokenType.BEARER);
      resp.setRefreshToken(entry.getRefreshToken());
      resp.setExpiresIn(token.getExpiresIn().getTime());
    } else {//auth code authorization
      resp.setCode(entry.getAuthCode());
      resp.setExpiresIn(entry.getCodeExpiresIn().getTime());
      resp.setRedirectURI(targetRedirectURI);
    }
    return resp;
  }
}
