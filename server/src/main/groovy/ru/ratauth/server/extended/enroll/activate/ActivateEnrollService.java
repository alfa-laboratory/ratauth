package ru.ratauth.server.extended.enroll.activate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.*;
import ru.ratauth.providers.auth.dto.ActivateInput;
import ru.ratauth.providers.auth.dto.ActivateResult;
import ru.ratauth.server.configuration.DestinationConfiguration;
import ru.ratauth.server.configuration.IdentityProvidersConfiguration;
import ru.ratauth.server.providers.IdentityProviderResolver;
import ru.ratauth.server.secutiry.TokenProcessor;
import ru.ratauth.server.services.AuthClientService;
import ru.ratauth.server.services.AuthSessionService;
import ru.ratauth.server.services.TokenCacheService;
import ru.ratauth.services.RestrictionService;
import rx.Observable;

import java.util.*;

import static java.util.Optional.ofNullable;
import static ru.ratauth.providers.Fields.USER_ID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ActivateEnrollService {

    private final AuthClientService clientService;
    private final AuthSessionService sessionService;
    private final TokenCacheService tokenCacheService;
    private final TokenProcessor tokenProcessor;
    private final IdentityProviderResolver identityProviderResolver;

    private final RestrictionService restrictionService;
    private final IdentityProvidersConfiguration identityProvidersConfiguration;

    public Observable<ActivateEnrollResponse> incAuthLevel(ActivateEnrollRequest request) {
        String mfa = request.getMfaToken();
        log.error("Activate incAuthLevel " + request);
        return Observable.zip(
                clientService.loadAndAuthRelyingParty(request.getClientId(), null, false),
                mfa != null ? sessionService.getByValidMFAToken(request.getMfaToken(), new Date()) : Observable.just(null),
                ImmutablePair::new
        )
                .flatMap(p -> {
                    checkAuthRestrictions(p.right, request);
                    return activateAndUpdateUserInfo(p.right, request, p.left);
                })
                .map(result -> {
                    return new ActivateEnrollResponse(request.getMfaToken(), result.getData());
                });
    }

    private Observable<Boolean> updateUserInfo(Session session, UserInfo userInfo, Set<String> scopes, Set<String> authContext) {
        return sessionService.updateIdToken(session, userInfo, scopes, authContext);
    }

    private Observable<ActivateResult> activateAndUpdateUserInfo(Session session, ActivateEnrollRequest request, RelyingParty relyingParty) {
        if (session != null) {
            Map<String, Object> tokenInfo = extractUserInfo(session);
            UserInfo userInfo = new UserInfo(tokenProcessor.filterUserInfo(tokenInfo));
            Set<String> authContext = tokenProcessor.extractAuthContext(tokenInfo);
            return activate(request, userInfo, relyingParty)
                    .flatMap(result -> updateUserInfo(session, userInfo.putAll(result.getData()), request.getScope(), authContext)
                            .map(b -> result));
        }
        return activate(request, null, relyingParty);
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


    private void checkAuthRestrictions(Session session, ActivateEnrollRequest request) {
        DestinationConfiguration restrictionConfiguration = identityProvidersConfiguration.getIdp().get(request.getEnroll().getFirst()).getRestrictions();
        String clientId = request.getClientId();

        log.error("activate check restrictions for " + session.toString() + " _________    " + session.getUserId());
        if (shouldIncrementRestrictionCount(restrictionConfiguration, clientId)) {
            restrictionService.checkIsAuthAllowed(clientId,
                    session.getUserId(),
                    (AcrValues) request.getEnroll(),
                    restrictionConfiguration.getAttemptMaxValue(),
                    restrictionConfiguration.getTtlInSeconds());
        }

    }

    private List<String> getClientIdRestriction(DestinationConfiguration restrictionConfiguration) {
        return restrictionConfiguration == null ? null : restrictionConfiguration.getClientId();
    }

    private boolean shouldIncrementRestrictionCount(DestinationConfiguration restrictionConfiguration, String requestClientId) {
        List<String> clientIdsRestriction = getClientIdRestriction(restrictionConfiguration);
        return clientIdsRestriction != null && clientIdsRestriction.contains(requestClientId);
    }
}
