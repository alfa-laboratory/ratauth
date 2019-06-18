package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.AuthClient;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.SessionClient;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.services.ClientService;
import rx.Observable;

import static ru.ratauth.utils.URIUtils.appendQuery;

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
                .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.CLIENT_NOT_FOUND)))
                .flatMap(this::checkBlockedAuthClient);
    }

    @Override
    public Observable<AuthClient> loadClient(String name) {
        return clientService.getClient(name)
                .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.CLIENT_NOT_FOUND)))
                .flatMap(this::checkBlockedAuthClient);
    }

    @Override
    public Observable<SessionClient> loadSessionClient(String name) {
        return clientService.getSessionClient(name)
                .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.CLIENT_NOT_FOUND)))
                .flatMap(this::checkBlockedAuthClient);
    }

    @Override
    public Observable<String> getAuthorizationPageURI(String name, String query) {
        return loadRelyingParty(name)
                .map(rp -> appendQuery(rp.getAuthorizationPageURI(), query));
    }

    @Override
    public Observable<String> getRegistrationPageURI(String name, String query) {
        return loadRelyingParty(name)
                .map(rp -> appendQuery(rp.getRegistrationPageURI(), query));
    }


    private <T extends AuthClient> Observable<T> checkBlockedAuthClient(T relyingParty) {
        return Observable.just(relyingParty).filter(rp -> AuthClient.Status.ACTIVE.equals(rp.getStatus()))
                .switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.CLIENT_BLOCKED)));
    }
}
