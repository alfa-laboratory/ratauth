package ru.ratauth.server.extended.enroll.verify;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.*;
import ru.ratauth.providers.auth.dto.VerifyInput;
import ru.ratauth.providers.auth.dto.VerifyResult;
import ru.ratauth.server.providers.IdentityProviderResolver;
import ru.ratauth.server.secutiry.TokenProcessor;
import ru.ratauth.server.services.AuthClientService;
import ru.ratauth.server.services.AuthSessionService;
import ru.ratauth.server.services.TokenCacheService;
import rx.Observable;

import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static ru.ratauth.server.utils.RedirectUtils.createRedirectURI;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class VerifyEnrollService {

    private final AuthClientService clientService;
    private final AuthSessionService sessionService;
    private final TokenCacheService tokenCacheService;
    private final TokenProcessor tokenProcessor;
    private final IdentityProviderResolver identityProviderResolver;

    @SneakyThrows
    private static RedirectResponse createResponse(Session session, RelyingParty relyingParty, VerifyEnrollRequest request, VerifyResult verifyResult) {
        AcrValues difference = request.getAuthContext().difference(request.getEnroll());
        if (difference.getValues().isEmpty()) {
            String authCode = session
                    .getEntry(relyingParty.getName())
                    .orElseThrow(() -> new IllegalStateException("sessionID = " + session.getId() + ", relyingParty = " + relyingParty))
                    .getAuthCode();
            return new SuccessResponse(createRedirectURI(relyingParty, request.getRedirectURI()), authCode);
        } else {

            String authorizationPageURI = relyingParty.getAuthorizationPageURI();
            URL url = new URL(authorizationPageURI);
            String pathWithEnroll = url.getPath().concat("/" + request.getAuthContext().getFirst());
            String redirectUrl = url.getHost() + pathWithEnroll + url.getQuery();

            return new NeedApprovalResponse(redirectUrl, request.getRedirectURI(), request.getMfaToken(), request.getClientId(), request.getScope(), request.getAuthContext());
        }
    }

    public Observable<RedirectResponse> incAuthLevel(VerifyEnrollRequest request) {
        return Observable.zip(
                clientService.loadAndAuthRelyingParty(request.getClientId(), null, false),
                sessionService.getByValidMFAToken(request.getMfaToken(), new Date()),
                ImmutablePair::new
        )
                .flatMap(p -> verifyAndUpdateUserInfo(p.right, request, p.left)
                        .map(result -> createResponse(p.right, p.left, request, result)));
    }

    private Observable<VerifyResult> verifyAndUpdateUserInfo(Session session, VerifyEnrollRequest request, RelyingParty relyingParty) {
        Map<String, Object> tokenInfo = extractUserInfo(session);
        UserInfo userInfo = new UserInfo(tokenProcessor.filterUserInfo(tokenInfo));
        Set<String> authContext = tokenProcessor.extractAuthContext(tokenInfo);

        return verify(request, userInfo, relyingParty)
                .doOnNext(result -> updateUserInfo(session, userInfo.putAll(result.getData()), request.getScope(), authContext));
    }

    private void updateUserInfo(Session session, UserInfo userInfo, Set<String> scopes, Set<String> authContext) {
        sessionService.updateIdToken(session, userInfo, scopes, authContext);
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
