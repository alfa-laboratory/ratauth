package ru.ratauth.server.extended.enroll.verify;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.*;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.exception.AuthorizationException.ID;
import ru.ratauth.exception.UpdateFlowException;
import ru.ratauth.providers.auth.dto.VerifyInput;
import ru.ratauth.providers.auth.dto.VerifyResult;
import ru.ratauth.providers.auth.dto.VerifyResult.Status;
import ru.ratauth.server.extended.common.RedirectResponse;
import ru.ratauth.server.extended.update.UpdateProcessResponse;
import ru.ratauth.server.providers.IdentityProviderResolver;
import ru.ratauth.server.secutiry.TokenProcessor;
import ru.ratauth.server.services.AuthClientService;
import ru.ratauth.server.services.AuthSessionService;
import ru.ratauth.server.services.DeviceService;
import ru.ratauth.server.services.TokenCacheService;
import ru.ratauth.server.utils.RedirectUtils;
import ru.ratauth.services.UpdateDataService;
import rx.Observable;
import rx.functions.Func1;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static ru.ratauth.server.utils.DateUtils.fromLocal;
import static ru.ratauth.server.utils.RedirectUtils.createRedirectURI;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class VerifyEnrollService {

    private final AuthClientService clientService;
    private final AuthSessionService sessionService;
    private final TokenCacheService tokenCacheService;
    private final UpdateDataService updateDataService;
    private final TokenProcessor tokenProcessor;
    private final IdentityProviderResolver identityProviderResolver;
    private final DeviceService deviceService;

    @SneakyThrows
    private Observable<RedirectResponse> createResponse(Session session, RelyingParty relyingParty, VerifyEnrollRequest request, VerifyResult verifyResult) {


        AcrValues difference = request.getAuthContext().difference(session.getReceivedAcrValues());
        if (difference.getValues().isEmpty()) {
            AuthEntry authEntry = session
                    .getEntry(relyingParty.getName())
                    .orElseThrow(() -> new IllegalStateException("sessionID = " + session.getId() + ", relyingParty = " + relyingParty));
            String username = (String) verifyResult.getData().get("username");
            if (verifyResult.getStatus().equals(Status.NEED_UPDATE)) {
                String reason = (String) verifyResult.getData().get("reason");
                String updateService = (String) verifyResult.getData().get("update_service");
                if (relyingParty.getUpdateRedirectURI() == null) {
                    throw new UpdateFlowException(UpdateFlowException.ID.UPDATE_URI_MISSING);
                }
                String redirectUri = relyingParty.getUpdateRedirectURI();
                boolean required = (Boolean) verifyResult.getData().get("required");

                return updateDataService.create(session.getId(), reason, updateService, redirectUri, required)
                        .flatMap(createUpdateCode(session))
                        .map(updateEntry -> createUpdateResponse(updateEntry, username));
            }

            Observable<RedirectResponse> updateDataObservable = updateDataService
                    .getUpdateData(session.getSessionToken())
                    .flatMap(createUpdateCode(session))
                    .map(updateEntry -> createUpdateResponse(updateEntry, username));

            return updateDataObservable
                    .switchIfEmpty(createSuccessResponse(request, relyingParty, authEntry));

        } else {

            String nextAcrValue = difference.getFirst();
            String redirectUrl = RedirectUtils.createRedirectUrl(relyingParty, nextAcrValue);
            return Observable.just(new NeedApprovalResponse(
                    redirectUrl, request.getRedirectURI(), request.getMfaToken(),
                    request.getClientId(), request.getScope(), request.getAuthContext()
            ));
        }

    }

    private static UpdateProcessResponse createUpdateResponse(UpdateDataEntry u, String username) {
        return new UpdateProcessResponse(u.getReason(), u.getCode(), u.getService(), u.getRedirectUri(), username);
    }

    private Func1<UpdateDataEntry, Observable<UpdateDataEntry>> createUpdateCode(Session session) {
        return updateDataEntry -> updateDataService
                .getCode(session.getSessionToken())
                .map(code -> {
                    updateDataEntry.setCode(code);
                    return updateDataEntry;
                });
    }

    private Observable<SuccessResponse> createSuccessResponse(VerifyEnrollRequest request, RelyingParty relyingParty, AuthEntry authEntry) {
        String authCode = authEntry.getAuthCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime authCodeExpiresIn = now.plus(relyingParty.getCodeTTL(), ChronoUnit.SECONDS);

        long expiresIn = ChronoUnit.SECONDS.between(now, authCodeExpiresIn);
        return sessionService.updateAuthCodeExpired(authCode, fromLocal(authCodeExpiresIn))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Observable.error(new AuthorizationException(ID.AUTH_CODE_EXPIRES_IN_UPDATE_FAILED)))
                .map(r -> new SuccessResponse(createRedirectURI(relyingParty, request.getRedirectURI()), authCode, expiresIn));
    }


    public Observable<RedirectResponse> incAuthLevel(VerifyEnrollRequest request) {
        return Observable.zip(
                clientService.loadAndAuthRelyingParty(request.getClientId(), null, false),
                sessionService.getByValidMFAToken(request.getMfaToken(), new Date()),
                ImmutablePair::new
        )
                .flatMap(p -> verifyAndUpdateUserInfo(p.right, request, p.left)
                        .flatMap(result -> {
                            Session session = p.right;
                            return createResponse(session, p.left, request, result)
                                    .map(response -> {
                                        response.putRedirectParameters("session_token", session.getSessionToken());
                                        return response;
                                    });
                        })
                        .flatMap(response -> {
                            if (response instanceof SuccessResponse) {
                                Session session = p.right;
                                return deviceService
                                        .sendDeviceInfo(
                                                request.getClientId(),
                                                Objects.toString(request.getAuthContext()),
                                                createDeviceInfoFromRequest(session, request),
                                                extractUserInfo(session)
                                        )
                                        .onErrorReturn(ex -> {
                                            log.error("Exception in jms", ex);
                                            return DeviceInfo.builder().build();
                                        })
                                        .map(it -> response);
                            }
                            return Observable.just(response);
                        })
                );
    }

    private DeviceInfo createDeviceInfoFromRequest(Session session, VerifyEnrollRequest request) {
        return DeviceInfo.builder()
                .userId(session.getUserId())
                .sessionToken(session.getSessionToken())
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


    private Observable<VerifyResult> verifyAndUpdateUserInfo(Session session, VerifyEnrollRequest request, RelyingParty relyingParty) {
        Map<String, Object> tokenInfo = extractUserInfo(session);
        UserInfo userInfo = new UserInfo(tokenProcessor.filterUserInfo(tokenInfo));
        Set<String> authContext = tokenProcessor.extractAuthContext(tokenInfo);

        return verify(request, userInfo, relyingParty)
                .flatMap(result -> updateUserInfo(session, request.getEnroll().getFirst(), userInfo.putAll(result.getData()), request.getScope(), authContext).map(b -> result));
    }

    private Observable<Boolean> updateUserInfo(Session session, String enroll, UserInfo userInfo, Set<String> scopes, Set<String> authContext) {
        AcrValues receivedAcrValues = session.getReceivedAcrValues();
        AcrValues newAcr = receivedAcrValues.add(enroll);
        session.setReceivedAcrValues(newAcr);
        return Observable.zip(
                sessionService.updateIdToken(session, userInfo, scopes, authContext),
                sessionService.updateAcrValues(session),
                (token, acr) -> token && acr);
    }

    private Map<String, Object> extractUserInfo(Session session) {
        return ofNullable(session)
                .map(Session::getUserInfo)
                .map(tokenCacheService::extractUserInfo)
                .orElseThrow(IllegalArgumentException::new);
    }

    private Observable<VerifyResult> verify(VerifyEnrollRequest request, UserInfo userInfo, RelyingParty relyingParty) {
        IdentityProvider identityProvider = identityProviderResolver.getProvider(relyingParty.getIdentityProvider());
        VerifyInput verifyInput = new VerifyInput(request.getData(), request.getEnroll(), userInfo, relyingParty.getName());
        return identityProvider.verify(verifyInput);
    }
}
