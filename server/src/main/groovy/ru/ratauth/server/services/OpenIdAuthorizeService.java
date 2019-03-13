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
import ru.ratauth.exception.ExpiredException;
import ru.ratauth.interaction.*;
import ru.ratauth.interaction.TokenType;
import ru.ratauth.providers.auth.dto.VerifyInput;
import ru.ratauth.providers.auth.dto.VerifyResult;
import ru.ratauth.providers.auth.dto.VerifyResult.Status;
import ru.ratauth.server.configuration.DestinationConfiguration;
import ru.ratauth.server.configuration.IdentityProvidersConfiguration;
import ru.ratauth.server.providers.IdentityProviderResolver;
import ru.ratauth.server.utils.RedirectUtils;
import ru.ratauth.services.RestrictionService;
import ru.ratauth.services.UpdateDataService;
import rx.Observable;
import rx.exceptions.Exceptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static ru.ratauth.providers.Fields.USER_ID;
import static ru.ratauth.providers.auth.dto.VerifyResult.Status.*;
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
    private final DeviceService deviceService;
    private final IdentityProviderResolver identityProviderResolver;
    private final UpdateDataService updateDataService;
    private final RestrictionService restrictionService;
    private final IdentityProvidersConfiguration identityProvidersConfiguration;


    @SneakyThrows
    private Observable<AuthzResponse> buildResponse(RelyingParty relyingParty, Session session, VerifyResult verifyResult, TokenCache tokenCache, AuthzRequest authzRequest) {
        String redirectUri = authzRequest.getRedirectURI();
        final String targetRedirectURI = createRedirectURI(relyingParty, redirectUri);
        //in case of autCode sent by authProvider
        if (session == null || CollectionUtils.isEmpty(session.getEntries())) {
            AuthzResponse resp = AuthzResponse.builder()
                    .location(relyingParty.getAuthorizationRedirectURI())
                    .data(verifyResult.getData())
                    .redirectURI(targetRedirectURI)
                    .build();
            return Observable.just(resp);
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
            return generateAuthCode(relyingParty, session, authzRequest, targetRedirectURI, entry, resp);
        }
        return Observable.just(resp);
    }

    private Observable<AuthzResponse> generateAuthCode(RelyingParty relyingParty, Session session, AuthzRequest authzRequest, String targetRedirectURI, AuthEntry entry, AuthzResponse resp) throws MalformedURLException {
        AcrValues acrValues = authzRequest.getAcrValues();

        if (isDefaultFlow(acrValues)) {
            log.debug("idp {}", relyingParty.getIdentityProvider());
            if (isDummyIdentityProvider(relyingParty)) {
                resp.setRedirectURI(targetRedirectURI);
                log.debug("targetRedirectURI = {}", targetRedirectURI);
            }
            return defaultFlow(entry, resp);
        }

        AcrValues receivedAcrValues = AcrValues.builder().acr(authzRequest.getEnroll()).build();
        AcrValues difference = acrValues.difference(receivedAcrValues);

        if (isReceivedRequiredAcrs(difference)) {

            return updateDataService
                    .getUpdateData(session.getSessionToken())
                    .flatMap(updateDataEntry -> updateDataService
                            .getCode(session.getSessionToken())
                            .map(code -> {
                                updateDataEntry.setCode(code);
                                return updateDataEntry;
                            })
                    )
                    .flatMap(updateDataEntry -> fillUpdateData(resp, relyingParty, updateDataEntry))
                    .switchIfEmpty(fillFinishAuthorization(targetRedirectURI, entry, resp));

        }

        return onNextAuthMethod(relyingParty, session, targetRedirectURI, resp, difference.getFirst());
    }

    private static boolean isDummyIdentityProvider(RelyingParty relyingParty) {
        return "DummyIdentityProvider".equals(relyingParty.getIdentityProvider());
    }

    private static boolean isDefaultFlow(AcrValues acrValues) {
        return acrValues == null;
    }

    private static Observable<AuthzResponse> defaultFlow(AuthEntry entry, AuthzResponse resp) {
        return Observable.just(resp)
                .map(r -> {
                    r.setCode(entry.getAuthCode());
                    r.setExpiresIn(entry.getCodeExpiresIn().getTime());
                    return r;
                });
    }

    private static boolean isReceivedRequiredAcrs(AcrValues difference) {
        return difference.getFirst() == null;
    }

    private static Observable<AuthzResponse> fillFinishAuthorization(String targetRedirectURI, AuthEntry entry, AuthzResponse resp) {
        return Observable.just(resp)
                .map(r -> {
                    r.setCode(entry.getAuthCode());
                    r.setExpiresIn(entry.getCodeExpiresIn().getTime());
                    r.setLocation(targetRedirectURI);
                    return resp;
                });
    }

    private static Observable<AuthzResponse> onNextAuthMethod(RelyingParty relyingParty, Session session, String targetRedirectURI, AuthzResponse resp, String firstAcr) throws MalformedURLException {
        return Observable.just(resp)
                .map(r -> {
                    try {
                        r.setLocation(createRedirectUrl(relyingParty, firstAcr));
                        r.setRedirectURI(targetRedirectURI);
                        r.setMfaToken(session.getMfaToken());
                        return r;
                    } catch (MalformedURLException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    @SneakyThrows
    private static Observable<AuthzResponse> fillUpdateData(AuthzResponse resp, RelyingParty relyingParty, UpdateDataEntry updateDataEntry) {
        return Observable.just(resp)
                .map(r -> {
                    try {
                        r.setReason(updateDataEntry.getReason());
                        r.setLocation(createRedirectUrl(relyingParty, updateDataEntry.getRedirectUri()));
                        r.setUpdateCode(updateDataEntry.getCode());
                        r.setUpdateService(updateDataEntry.getService());
                        return r;
                    } catch (MalformedURLException e) {
                        throw Exceptions.propagate(e);
                    }
                });
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
        return clientService.loadAndAuthRelyingParty(request.getClientId(), request.getClientSecret(), isAuthRequired(request))
                .flatMap(rp -> authenticateUser(request.getAuthData(), request.getAcrValues(), rp.getIdentityProvider(), rp.getName())
                        .map(request::addVerifyResultAcrToRequest)
                        .map(authRes -> {
                            List<DestinationConfiguration> restrictionConfigurations = new ArrayList<>();
                            restrictionConfigurations.add(identityProvidersConfiguration.getIdp().get(request.getAcrValues().getFirst()).getRestrictions());
                            restrictionConfigurations.add(identityProvidersConfiguration.getIdp().get(request.getAcrValues().getSecond()).getRestrictions());
                            String clientId = request.getClientId();
                            List<String> clientIdRestriction  = null;
                            for(DestinationConfiguration restrictionConfiguration: restrictionConfigurations) {
                                if (restrictionConfiguration != null) {
                                    clientIdRestriction = restrictionConfiguration.getClientId();
                                }
                                if (clientIdRestriction != null && clientIdRestriction.contains(clientId)) {
                                    restrictionService.checkIsAuthAllowed(clientId,
                                            authRes.getData().get(USER_ID.val()).toString(),
                                            authRes.getAcrValues(),
                                            restrictionConfiguration.getAttemptMaxValue(),
                                            restrictionConfiguration.getTtlInSeconds());
                                }
                            }
                            return new ImmutableTriple<>(rp, authRes, request.getAcrValues());
                        }))
                .flatMap(rpAuth -> createSession(request, rpAuth.getMiddle(), rpAuth.getRight(), rpAuth.getLeft())
                        .flatMap(session ->
                                createUpdateToken(rpAuth.middle, session, rpAuth.left)
                                        .map((entry) -> session)
                        )
                        .doOnNext(sessionService::updateAcrValues)
                        .flatMap(session -> createIdToken(rpAuth.left, session, rpAuth.right)
                                .flatMap(idToken -> buildResponse(rpAuth.left, session, rpAuth.middle, idToken, request))
                                .flatMap(authzResponse -> {
                                    if (authzResponse.getCode() != null || authzResponse.getUpdateCode() != null) {
                                        return deviceService
                                                .resolveDeviceInfo(
                                                        request.getClientId(),
                                                        Objects.toString(request.getAcrValues()),
                                                        createDeviceInfoFromRequest(session, request),
                                                        extractUserInfo(session)
                                                )
                                                .map(it -> authzResponse);
                                    }
                                    return Observable.just(authzResponse);
                                })
                        ))
                .doOnCompleted(() -> log.info("Authorization succeed"));
    }

    private Observable<Boolean> createUpdateToken(VerifyResult verifyResult, Session session, RelyingParty relyingParty) {
        if (Status.NEED_UPDATE.equals(verifyResult.getStatus())) {
            return updateDataService.create(session.getSessionToken(),
                    (String) verifyResult.getData().get("reason"),
                    (String) verifyResult.getData().get("update_service"),
                    relyingParty.getUpdateRedirectURI(),
                    (Boolean) verifyResult.getData().get("required"))
                    .map(entry -> Boolean.TRUE);
        }
        return Observable.just(Boolean.FALSE);
    }

    private boolean isAuthRequired(AuthzRequest request) {
        return request.getResponseType() != AuthzResponseType.CODE;
    }

    private Map<String, Object> extractUserInfo(Session session) {
        return ofNullable(session)
                .map(Session::getUserInfo)
                .map(tokenCacheService::extractUserInfo)
                .orElseThrow(IllegalArgumentException::new);
    }


    private DeviceInfo createDeviceInfoFromRequest(Session session, AuthzRequest request) {
        return DeviceInfo.builder()
                .userId(session.getUserId())
                .deviceAppVersion(request.getDeviceAppVersion())
                .deviceId(request.getDeviceId())
                .deviceUUID(request.getDeviceUUID())
                .deviceModel(request.getDeviceModel())
                .deviceGeo(request.getDeviceGeo())
                .deviceLocale(request.getDeviceLocale())
                .deviceCity(request.getDeviceCity())
                .deviceName(request.getDeviceName())
                .deviceOSVersion(request.getDeviceOSVersion())
                .deviceBootTime(request.getDeviceBootTime())
                .deviceTimezone(request.getDeviceTimezone())
                .deviceIp(request.getDeviceIp())
                .deviceUserAgent(request.getDeviceUserAgent())
                .creationDate(new Date())
                .build();
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
            sessionObs = sessionService.getByValidSessionToken(request.getSessionToken(), new Date(), true);
            authClientObs = clientService.loadAndAuthSessionClient(request.getClientId(), request.getClientSecret(), true);
        }

        return Observable.zip(
                authClientObs
                        .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.CREDENTIALS_WRONG))),
                clientService.loadRelyingParty(request.getExternalClientId())
                        .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.CLIENT_NOT_FOUND))),
                sessionObs
                        .switchIfEmpty(Observable.error(new ExpiredException(ExpiredException.ID.TOKEN_EXPIRED))),
                (oldRP, newRP, session) -> new ImmutablePair<>(newRP, session)
        ).flatMap(rpSession -> {
                    String redirectURI = rpSession.getLeft().getAuthorizationRedirectURI();
                    return sessionService.addEntry(rpSession.getRight(), rpSession.getLeft(), request.getScopes(), redirectURI)
                            .flatMap(session -> buildResponse(rpSession.getLeft(), session,
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
        if (isVerifyStatusPositive(verifyResult)) {
            if (AuthzResponseType.TOKEN == oauthRequest.getResponseType()) {//implicit auth
                return sessionService.createSession(relyingParty, verifyResult.getData(), oauthRequest.getScopes(), acrValues, oauthRequest.getRedirectURI());
            } else {//auth code auth
                return sessionService.initSession(relyingParty, verifyResult.getData(), oauthRequest.getScopes(), acrValues, oauthRequest.getRedirectURI());
            }
        } else {
            return Observable.just(new Session());//authCode provided by external Auth provider
        }
    }

    private boolean isVerifyStatusPositive(VerifyResult verifyResult) {
        return SUCCESS == verifyResult.getStatus() || NEED_UPDATE == verifyResult.getStatus();
    }

    private Observable<VerifyResult> authenticateUser(Map<String, String> authData, AcrValues enroll, String identityProviderName, String relyingPartyName) {

        IdentityProvider provider = identityProviderResolver.getProvider(identityProviderName);
        VerifyInput verifyInput = new VerifyInput(authData, enroll, new UserInfo(), relyingPartyName);
        return provider.verify(verifyInput);
    }


}
