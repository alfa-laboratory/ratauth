package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.exception.RegistrationException;
import ru.ratauth.interaction.RegistrationRequest;
import ru.ratauth.interaction.RegistrationResponse;
import ru.ratauth.interaction.TokenResponse;
import ru.ratauth.providers.registrations.RegistrationProvider;
import ru.ratauth.providers.registrations.dto.RegInput;
import ru.ratauth.providers.registrations.dto.RegResult;
import ru.ratauth.utils.StringUtils;
import ru.ratauth.utils.URIUtils;
import rx.Observable;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 29/01/16
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdRegistrationService implements RegistrationService {
    private final Map<String, RegistrationProvider> registerProviders;
    private final AuthTokenService tokenService;
    private final AuthSessionService authSessionService;
    private final AuthClientService authClientService;

    @Override
    public Observable<RegistrationResponse> register(RegistrationRequest request) {
        return authClientService.loadRelyingParty(request.getClientId())
                .flatMap(rp -> registerProviders.get(rp.getIdentityProvider())
                        .register(RegInput.builder().relyingParty(rp.getName()).data(request.getData()).build())
                        .map(result -> convertToResponse(result, rp, request))
                )
                .doOnCompleted(() -> log.info("First step of registration succeed"));
    }

    @Override
    public Observable<TokenResponse> finishRegister(RegistrationRequest request) {
        return authClientService.loadAndAuthRelyingParty(request.getClientId(), request.getClientSecret(), true)
                .flatMap(rp -> registerProviders.get(rp.getIdentityProvider())
                        .register(RegInput.builder().relyingParty(rp.getName()).data(request.getData()).build())
                        .map(regResult -> new ImmutablePair<>(rp, regResult)))
                //TODO@ruslan разделить смс и авторизацию
                .flatMap(rpRegResult -> authSessionService.createSession(rpRegResult.getLeft(), rpRegResult.getRight().getData(), request.getScopes(), rpRegResult.getRight().getAcrValues(), null)
                        .map(session -> new ImmutablePair<>(rpRegResult.getLeft(), session)))
                .flatMap(rpSession -> tokenService.createIdTokenAndResponse(rpSession.getRight(), rpSession.getLeft()))
                .doOnCompleted(() -> log.info("Second step of registration succeed"));
    }

    private RegistrationResponse convertToResponse(RegResult regResult, RelyingParty relyingParty, RegistrationRequest request) {
        String redirectURI = request.getRedirectURI();
        if (StringUtils.isBlank(redirectURI)) {
            redirectURI = relyingParty.getRegistrationRedirectURI();
        } else {
            if (!URIUtils.compareHosts(redirectURI, relyingParty.getRedirectURIs()))
                throw new RegistrationException(RegistrationException.ID.REDIRECT_NOT_CORRECT);
        }
        return RegistrationResponse.builder()
                .data(regResult.getData())
                .redirectUrl(redirectURI)
                .build();
    }
}
