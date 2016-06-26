package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.interaction.GrantType;
import ru.ratauth.interaction.RegistrationRequest;
import ru.ratauth.interaction.TokenResponse;
import ru.ratauth.providers.registrations.RegistrationProvider;
import ru.ratauth.providers.registrations.dto.RegInput;
import ru.ratauth.providers.registrations.dto.RegResult;
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
  public Observable<RegResult> register(RegistrationRequest request) {
    return authClientService.loadRelyingParty(request.getClientId())
        .flatMap(rp -> registerProviders.get(rp.getIdentityProvider())
                .register(RegInput.builder().relyingParty(rp.getName()).data(request.getData()).build()))
        .doOnCompleted(() -> log.info("First step of registration succeed"));
  }

  @Override
  public Observable<TokenResponse> finishRegister(RegistrationRequest request) {
    return authClientService.loadAndAuthRelyingParty(request.getClientId(), request.getClientSecret(), GrantType.AUTHORIZATION_CODE != request.getGrantType())
        .flatMap(rp -> registerProviders.get(rp.getIdentityProvider())
                .register(RegInput.builder().relyingParty(rp.getName()).data(request.getData()).build())
                .map(regResult -> new ImmutablePair<>(rp, regResult)))
        .flatMap(rpRegResult -> authSessionService.createSession(rpRegResult.getLeft(), rpRegResult.getRight().getData(), request.getScopes(), null)
                .map(session -> new ImmutablePair<>(rpRegResult.getLeft(), session)))
        .flatMap(rpSession -> tokenService.createIdTokenAndResponse(rpSession.getRight(), rpSession.getLeft()))
        .doOnCompleted(() -> log.info("Second step of registration succeed"));
  }
}
