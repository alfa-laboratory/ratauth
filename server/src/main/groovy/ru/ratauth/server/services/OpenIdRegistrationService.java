package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.interaction.AuthzResponse;
import ru.ratauth.interaction.RegistrationRequest;
import ru.ratauth.providers.registrations.RegistrationProvider;
import ru.ratauth.providers.registrations.dto.RegInput;
import ru.ratauth.providers.registrations.dto.RegResult;
import ru.ratauth.services.RelyingPartyService;
import rx.Observable;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 29/01/16
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdRegistrationService implements RegistrationService {
  private final RelyingPartyService relyingPartyService;
  private final Map<String, RegistrationProvider> registerProviders;

  @Override
  public Observable<RegResult> register(RegistrationRequest request) {
    final RelyingParty relyingParty = relyingPartyService.getRelyingParty(request.getClientId())
        .switchIfEmpty(Observable.error(new AuthorizationException("RelyingParty not found")))
        .toBlocking().single();

    return registerProviders.get(relyingParty.getIdentityProvider())
        .register(RegInput.builder().relyingParty(relyingParty.getName()).data(request.getData()).build());
  }
}
