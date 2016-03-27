package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.AuthClient;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.services.ClientService;
import rx.Observable;

/**
 * @author mgorelikov
 * @since 16/02/16
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenIdClientService implements AuthClientService {
  private final ClientService clientService;

  @Override
  public Observable<RelyingParty> loadRelyingParty(String name) {
    return clientService.getRelyingParty(name)
        .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.CLIENT_NOT_FOUND)));
  }

  @Override
  public Observable<AuthClient> loadClient(String name) {
    return clientService.getClient(name)
        .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.CLIENT_NOT_FOUND)));
  }
}
