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
import ru.ratauth.server.configuration.SessionConfiguration;
import ru.ratauth.server.secutiry.OAuthSystemException;
import rx.Observable;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static ru.ratauth.interaction.GrantType.*;

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
    private final SessionConfiguration sessionConfiguration;

    @Override
    @SneakyThrows
    public Observable<TokenResponse> getToken(TokenRequest oauthRequest) throws OAuthSystemException, JOSEException {
        final Observable<RelyingParty> relyingPartyObservable = clientService.loadAndAuthRelyingParty(oauthRequest.getClientId(), oauthRequest.getClientSecret(), true);
        return relyingPartyObservable
                .flatMap(rp -> loadSession(oauthRequest, rp).map(ses -> new ImmutablePair<>(rp, ses)))
                .flatMap(rpSess -> authSessionService.addToken(oauthRequest, rpSess.getRight(), rpSess.getLeft()).map(res -> rpSess))
                .flatMap(rpSess -> createIdTokenAndResponse(rpSess.getRight(), rpSess.getLeft()))
                .doOnCompleted(() -> log.info("Get-token succeed"));
    }

    @Override
    public Observable<TokenResponse> createIdTokenAndResponse(Session session, RelyingParty relyingParty) {
        AuthEntry entry = session.getEntry(relyingParty.getName()).get();
        return tokenCacheService.getToken(session, relyingParty, entry)
                .map(idToken -> new ImmutablePair<>(entry, idToken))
                .map(entryToken -> convertToResponse(entryToken.getLeft(), entryToken.getRight().getIdToken(), session.getSessionToken()))
                .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.TOKEN_NOT_FOUND)));
    }

    @Override
    public Observable<TokenResponse> createIdTokenAndResponse(Session session, RelyingParty relyingParty, String authContext) {
        AuthEntry entry = session.getEntry(relyingParty.getName()).get();
        return tokenCacheService.getToken(session, relyingParty, entry)
                .map(idToken -> new ImmutablePair<>(entry, idToken))
                .map(entryToken -> convertToResponse(entryToken.getLeft(), entryToken.getRight().getIdToken(), session.getSessionToken()))
                .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.TOKEN_NOT_FOUND)));
    }

    @Override
    public Observable<CheckTokenResponse> checkToken(CheckTokenRequest oauthRequest) {
        // check basic auth first
        Observable<AuthClient> authClient = loadRelyingParty(oauthRequest);
        authClient.subscribe();
        return authSessionService.getByValidToken(oauthRequest.getToken(), new Date())
                .doOnNext(sessionStatusChecker::checkAndUpdateSession)
                .zipWith(authClient, ImmutablePair::new)
                .doOnNext(sessionClient -> checkSession(sessionClient.getLeft(), sessionClient.getRight()))
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
                            .build();
                })
                .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.TOKEN_NOT_FOUND)))
                .doOnCompleted(() -> log.info("Check token succeed"));
    }

    private void checkSession(Session session, AuthClient authClient) {

        if (!session.getReceivedAcrValues().getValues().containsAll(Arrays.asList("sms"))
                && !session.getReceivedAcrValues().getValues().containsAll(Arrays.asList("ib-username-password"))
                && !session.getReceivedAcrValues().getValues().containsAll(Arrays.asList("upupcard"))
                && !session.getReceivedAcrValues().getValues().containsAll(Arrays.asList("username"))
                && !session.getReceivedAcrValues().getValues().containsAll(Arrays.asList("ad-username-password"))
                && !session.getReceivedAcrValues().getValues().containsAll(Arrays.asList("not-client-sms"))
                && !session.getReceivedAcrValues().getValues().containsAll(Arrays.asList("corp-username"))
                && !"private-vr-api".equals(authClient.getName())) {
            log.error("Invalid acr values: " + session.getReceivedAcrValues());
            throw new AuthorizationException(AuthorizationException.ID.INVALID_ACR_VALUES);
        }
        if (sessionConfiguration.isNeedToCheckSession() && (Status.BLOCKED == session.getStatus() || Status.LOGGED_OUT == session.getStatus()))
            throw new AuthorizationException(AuthorizationException.ID.SESSION_BLOCKED);
        if (sessionConfiguration.isNeedToCheckSession() && session.getExpiresIn().before(new Date()))
            throw new ExpiredException(ExpiredException.ID.SESSION_EXPIRED);
    }

    private TokenResponse convertToResponse(AuthEntry authEntry, String idToken, String sessionToken) {
        final Token token = authEntry.getLatestToken().get();
        return TokenResponse.builder()
                .accessToken(token.getToken())
                .expiresIn(token.getExpiresIn().getTime())
                .tokenType(TokenType.BEARER.toString())
                .idToken(idToken)
                .refreshToken(token.getRefreshToken())
                .clientId(authEntry.getRelyingParty())
                .sessionToken(sessionToken)
                .build();
    }

    private Observable<Session> loadSession(TokenRequest oauthRequest, RelyingParty relyingParty) {
        Observable<Session> authObs;
        if (oauthRequest.getGrantType() == AUTHORIZATION_CODE) {
            authObs = authSessionService
                    .getByValidCode(oauthRequest.getAuthzCode(), new Date())
                    .filter(session -> session.getEntry(relyingParty.getName())
                            .map(entry -> CollectionUtils.isEmpty(entry.getTokens()))
                            .orElse(false));
        } else if (oauthRequest.getGrantType() == REFRESH_TOKEN || oauthRequest.getGrantType() == AUTHENTICATION_TOKEN) {
            authObs = authSessionService.getByValidRefreshToken(oauthRequest.getRefreshToken(), new Date());
        } else {
            return Observable.error(new AuthorizationException(AuthorizationException.ID.INVALID_GRANT_TYPE));
        }

        return authObs
                .map(session -> {
                    checkSession(session, relyingParty);
                    return session;
                })
                .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.SESSION_NOT_FOUND)));
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
