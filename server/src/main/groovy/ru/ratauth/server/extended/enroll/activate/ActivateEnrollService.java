package ru.ratauth.server.extended.enroll.activate;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.*;
import ru.ratauth.providers.auth.dto.ActivateInput;
import ru.ratauth.providers.auth.dto.ActivateResult;
import ru.ratauth.server.extended.restriction.CheckRestrictionService;
import ru.ratauth.server.providers.IdentityProviderResolver;
import ru.ratauth.server.secutiry.TokenProcessor;
import ru.ratauth.server.services.AuthClientService;
import ru.ratauth.server.services.AuthSessionService;
import ru.ratauth.server.services.DeviceService;
import ru.ratauth.server.services.TokenCacheService;
import rx.Observable;

import java.util.*;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ActivateEnrollService {

    private final AuthClientService clientService;
    private final AuthSessionService sessionService;
    private final TokenCacheService tokenCacheService;
    private final TokenProcessor tokenProcessor;
    private final DeviceService deviceService;
    private final IdentityProviderResolver identityProviderResolver;
    private final CheckRestrictionService checkRestrictionService;

    public Observable<ActivateEnrollResponse> incAuthLevel(ActivateEnrollRequest request) {
        String mfa = request.getMfaToken();
        return Observable.zip(
                clientService.loadAndAuthRelyingParty(request.getClientId(), null, false),
                mfa != null ? sessionService.getByValidMFAToken(request.getMfaToken(), new Date()) : Observable.just(null),
                ImmutablePair::new
        )
                .flatMap(p -> activateAndUpdateUserInfo(p.right, request, p.left))
                .map(result -> new ActivateEnrollResponse(request.getMfaToken(), result.getData()));
    }

    private Observable<Boolean> updateUserInfo(Session session, UserInfo userInfo, Set<String> scopes, Set<String> authContext) {
        return sessionService.updateIdToken(session, userInfo, scopes, authContext);
    }

    private Observable<ActivateResult> activateAndUpdateUserInfo(Session session, ActivateEnrollRequest request, RelyingParty relyingParty) {
        if (session != null) {
            checkRestrictionService.checkAuthRestrictions(session, request);
            saveDeviceInfoInformation(session, request);
            Map<String, Object> tokenInfo = extractUserInfo(session);
            UserInfo userInfo = new UserInfo(tokenProcessor.filterUserInfo(tokenInfo));
            Set<String> authContext = tokenProcessor.extractAuthContext(tokenInfo);
            return activate(request, userInfo, relyingParty)
                    .flatMap(result -> updateUserInfo(session, userInfo.putAll(result.getData()), request.getScope(), authContext)
                            .map(b -> result));
        }
        return activate(request, null, relyingParty);
    }

    private void saveDeviceInfoInformation(Session session, ActivateEnrollRequest request) {
        deviceService.saveDeviceInfo(
                request.getClientId(),
                Objects.toString(request.getEnroll()),
                createDeviceInfoFromRequest(session, request),
                extractUserInfo(session)
        ).subscribe();
    }

    private DeviceInfo createDeviceInfoFromRequest(Session session, ActivateEnrollRequest request) {
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

    @SuppressWarnings("unchecked")
    private Set<String> extractScopeFromTokenInfo(Map<String, Object> tokenInfo) {
        return tokenInfo.get("scope") == null ? (Set<String>) tokenInfo.get("scope") : Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractAuthContextFromTokenInfo(Map<String, Object> tokenInfo) {
        return tokenInfo.get("acr_values") == null ? (Set<String>) tokenInfo.get("acr_values") : Collections.emptySet();
    }

    private Map<String, Object> extractUserInfo(Session session) {
        return ofNullable(session)
                .map(Session::getUserInfo)
                .map(tokenCacheService::extractUserInfo)
                .orElseThrow(IllegalArgumentException::new);
    }

    private Observable<ActivateResult> activate(ActivateEnrollRequest request, UserInfo userInfo, RelyingParty relyingParty) {
        IdentityProvider identityProvider = identityProviderResolver.getProvider(relyingParty.getIdentityProvider());
        ActivateInput activateInput = new ActivateInput(request.getData(), request.getEnroll(), userInfo, relyingParty.getName());
        return identityProvider.activate(activateInput);
    }

}
