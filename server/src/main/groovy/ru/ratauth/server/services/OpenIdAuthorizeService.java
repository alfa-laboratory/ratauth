package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.ratauth.entities.AcrValues;
import ru.ratauth.entities.AuthClient;
import ru.ratauth.entities.AuthEntry;
import ru.ratauth.entities.IdentityProvider;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Session;
import ru.ratauth.entities.Token;
import ru.ratauth.entities.TokenCache;
import ru.ratauth.entities.UserInfo;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.interaction.AuthzRequest;
import ru.ratauth.interaction.AuthzResponse;
import ru.ratauth.interaction.AuthzResponseType;
import ru.ratauth.interaction.GrantType;
import ru.ratauth.interaction.TokenType;
import ru.ratauth.providers.auth.dto.VerifyInput;
import ru.ratauth.providers.auth.dto.VerifyResult;
import ru.ratauth.server.providers.IdentityProviderResolver;
import ru.ratauth.server.utils.RedirectUtils;
import rx.Observable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.isNoneBlank;
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

    @SneakyThrows
    private static AuthzResponse buildResponse(RelyingParty relyingParty, Session session, VerifyResult verifyResult, TokenCache tokenCache, AuthzRequest authzRequest) {
        String redirectUri = authzRequest.getRedirectURI();
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
                .sessionToken(session.getSessionToken())
                .acrValues(verifyResult.getAcrValues())
                .data(verifyResult.getData())
                .build();
        final Optional<Token> tokenOptional = entry.getLatestToken();
        //implicit auth
        if (tokenOptional.isPresent()) {
            final Token token = tokenOptional.get();
            resp.setToken(token.getToken());
            if (tokenCache != null)
                resp.setIdToken(tokenCache.getIdToken());
            resp.setTokenType(TokenType.BEARER);
            resp.setRefreshToken(token.getRefreshToken());
            resp.setExpiresIn(token.getExpiresIn().getTime());
        } else {
            generateAuthCode(relyingParty, session, authzRequest, targetRedirectURI, entry, resp);
        }
        return resp;
    }

    private static void generateAuthCode(RelyingParty relyingParty, Session session, AuthzRequest authzRequest, String targetRedirectURI, AuthEntry entry, AuthzResponse resp) throws MalformedURLException {
        AcrValues acrValues = authzRequest.getAcrValues();

        if (isDefaultFlow(acrValues)) {
            defaultFlow(entry, resp);
            return;
        }

        AcrValues receivedAcrValues = AcrValues.builder().acr(authzRequest.getEnroll()).build();
        AcrValues difference = acrValues.difference(receivedAcrValues);

        if (isReceivedRequiredAcrs(difference)) {
            onFinishAuthorization(targetRedirectURI, entry, resp);
            return;
        }

        onNextAuthMethod(relyingParty, session, targetRedirectURI, resp, difference.getFirst());
    }

    private static boolean isDefaultFlow(AcrValues acrValues) {
        return acrValues == null;
    }

    private static void defaultFlow(AuthEntry entry, AuthzResponse resp) {
        resp.setCode(entry.getAuthCode());
        resp.setExpiresIn(entry.getCodeExpiresIn().getTime());
    }

    private static boolean isReceivedRequiredAcrs(AcrValues difference) {
        return difference.getFirst() == null;
    }

    private static void onFinishAuthorization(String targetRedirectURI, AuthEntry entry, AuthzResponse resp) {
        resp.setCode(entry.getAuthCode());
        resp.setExpiresIn(entry.getCodeExpiresIn().getTime());
        resp.setLocation(targetRedirectURI);
    }

    private static void onNextAuthMethod(RelyingParty relyingParty, Session session, String targetRedirectURI, AuthzResponse resp, String firstAcr) throws MalformedURLException {
        String resultLocation = createRedirectUrl(relyingParty, firstAcr);
        resp.setLocation(resultLocation);
        resp.setRedirectURI(targetRedirectURI);
        resp.setMfaToken(session.getMfaToken());
    }

    private static String createRedirectUrl(RelyingParty relyingParty, String firstAcr) throws MalformedURLException {
        URL url = new URL(relyingParty.getAuthorizationPageURI());
        return RedirectUtils.createRedirectURI(
                url.getProtocol() + "://" + url.getHost() + url.getPath() + "/" + firstAcr,
                url.getQuery()
        );
    }

    private static Function<String, Function<String, Function<String, String>>> addToPathIfExistCurry() {
        return path -> sign -> parameter -> isNoneBlank(parameter) ? path + sign + parameter : path;
    }

    @Override
    @SneakyThrows
    public Observable<AuthzResponse> authenticate(AuthzRequest request) {
        return clientService.loadAndAuthRelyingParty(request.getClientId(), request.getClientSecret(),
                request.getResponseType() != AuthzResponseType.CODE)
                .flatMap(rp ->
                        authenticateUser(request.getAuthData(), request.getAcrValues(), rp.getIdentityProvider(), rp.getName())
                                .map(request::addVerifyResultAcrToRequest)
                                .map(authRes -> new ImmutableTriple<>(rp, authRes, request.getAcrValues())))
                .flatMap(rpAuth ->
                        createSession(request, rpAuth.getMiddle(), rpAuth.getRight(), rpAuth.getLeft())
                                .map(session -> {
                                    sessionService.updateAcrValues(session).toBlocking().single();
                                    return session;
                                })
                                .flatMap(session -> createIdToken(rpAuth.left, session, rpAuth.right)
                                        .map(idToken -> buildResponse(rpAuth.left, session, rpAuth.middle, idToken, request))))
                .doOnCompleted(() -> log.info("Authorization succeed"));
    }

    //TODO check token relation
    @Override
    public Observable<AuthzResponse> crossAuthenticate(AuthzRequest request) {
        Observable<Session> sessionObs;
        Observable<? extends AuthClient> authClientObs;
        if (GrantType.AUTHENTICATION_TOKEN == request.getGrantType()) {
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
                                    VerifyResult.builder()
                                            .data(Collections.emptyMap())
                                            .status(NEED_APPROVAL)
                                            .build(), null, request));
                }
        ).doOnCompleted(() -> log.info("Cross-authorization succeed"));
    }

    private Observable<TokenCache> createIdToken(RelyingParty relyingParty, Session session, AcrValues acrValues) {
        Optional<AuthEntry> entry = session.getEntry(relyingParty.getName());
        Optional<Token> token = entry.flatMap(AuthEntry::getLatestToken);
        if (token.isPresent()) {
            assert entry.isPresent();
            return tokenCacheService.getToken(session, relyingParty, entry.get());
        } else
            return Observable.just((TokenCache) null);
    }

    private Observable<Session> createSession(AuthzRequest oauthRequest, VerifyResult verifyResult, AcrValues acrValues, RelyingParty relyingParty) {
        if (VerifyResult.Status.SUCCESS == verifyResult.getStatus()) {
            if (AuthzResponseType.TOKEN == oauthRequest.getResponseType()) {//implicit auth
                return sessionService.createSession(relyingParty, verifyResult.getData(), oauthRequest.getScopes(), acrValues, oauthRequest.getRedirectURI());
            } else {//auth code auth
                return sessionService.initSession(relyingParty, verifyResult.getData(), oauthRequest.getScopes(), acrValues, oauthRequest.getRedirectURI());
            }
        } else {
            return Observable.just(new Session());//authCode provided by external Auth provider
        }
    }

    private Observable<VerifyResult> authenticateUser(Map<String, String> authData, AcrValues enroll, String identityProviderName, String relyingPartyName) {
        IdentityProvider provider = identityProviderResolver.getProvider(identityProviderName);
        VerifyInput verifyInput = new VerifyInput(authData, enroll, new UserInfo(), relyingPartyName);
        return provider.verify(verifyInput);
    }
}
