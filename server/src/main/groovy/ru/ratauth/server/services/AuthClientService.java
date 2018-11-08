package ru.ratauth.server.services;

import ru.ratauth.entities.AuthClient;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.SessionClient;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.server.utils.SecurityUtils;
import rx.Observable;

/**
 * Class for loading information about authServer clients(relying party or resource server)
 *
 * @author mgorelikov
 * @since 16/02/16
 */
public interface AuthClientService {

    /**
     * Just loads relyingParty by name
     *
     * @param name unique name
     * @return observable of loaded AuthClient
     */
    Observable<RelyingParty> loadRelyingParty(String name);

    /**
     * Just loads client by name
     *
     * @param name unique client name
     * @return observable of loaded AuthClient
     */
    Observable<AuthClient> loadClient(String name);

    /**
     * Just loads sessionClient by name
     *
     * @param name unique name
     * @return observable of loaded sessionClient
     */
    Observable<SessionClient> loadSessionClient(String name);

    /**
     * Loads relying party authPageURI from database and appends query to it
     *
     * @param name  unique client name
     * @param query input query string
     * @return authorizationPageURI
     */
    Observable<String> getAuthorizationPageURI(String name, String query);

    /**
     * Loads relying party reisterPageURI from database and appends query to it
     *
     * @param name  unique client name
     * @param query input query string
     * @return registrationPageURI
     */
    Observable<String> getRegistrationPageURI(String name, String query);

    /**
     * Loads Relying party by name and checks it password in case of auth required
     *
     * @param name         unique name
     * @param password     relying party password
     * @param authRequired flag that auth required
     * @return observable of loaded relyingParty
     */
    default Observable<RelyingParty> loadAndAuthRelyingParty(String name, String password, boolean authRequired) {
        Observable<RelyingParty> relyingPartyObservable = loadRelyingParty(name);
        return addAuth(relyingPartyObservable, password, authRequired);
    }

    /**
     * Loads AuthClient by name and checks it password in case of auth required
     *
     * @param name         unique name
     * @param password     AuthClient password
     * @param authRequired flag that auth required
     * @return observable of loaded authClient
     */
    default Observable<AuthClient> loadAndAuthClient(String name, String password, boolean authRequired) {
        Observable<AuthClient> authClientObservable = loadClient(name);
        return addAuth(authClientObservable, password, authRequired);
    }

    /**
     * Loads Session client by name and checks it password in case of auth required
     *
     * @param name         unique name
     * @param password     session client password
     * @param authRequired flag that auth required
     * @return observable of loaded sessionClient
     */
    default Observable<SessionClient> loadAndAuthSessionClient(String name, String password, boolean authRequired) {
        Observable<SessionClient> sessionClientObservable = loadSessionClient(name);
        return addAuth(sessionClientObservable, password, authRequired);
    }

    /**
     * Adds credentials check filter to the input observable
     *
     * @param clientObservable input observable
     * @param password         client password
     * @param authRequired     credential check flag
     * @return modified observable
     */
    default <T extends AuthClient> Observable<T> addAuth(Observable<T> clientObservable, String password, boolean authRequired) {
        return clientObservable.filter(rp -> !authRequired ||
                SecurityUtils.hashPassword(password, rp.getSalt()).equalsIgnoreCase(rp.getPassword())
        ).switchIfEmpty(Observable.error(new AuthorizationException(AuthorizationException.ID.CLIENT_NOT_FOUND)));
    }
}
