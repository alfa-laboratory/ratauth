package ru.ratauth.server.services;

import ru.ratauth.entities.OpenIdConnectDiscoveryProperties;
import ru.ratauth.services.OpenIdConnectDiscoveryService;
import rx.Observable;

/**
 * Created by sserdyuk on 9/11/17.
 */

public class OpenIdDefaultDiscoveryService implements OpenIdConnectDiscoveryService {

    public OpenIdDefaultDiscoveryService(OpenIdConnectDiscoveryProperties discoveryProperties) {
        this.discoveryProperties = discoveryProperties;
    }

    private final OpenIdConnectDiscoveryProperties discoveryProperties;

    @Override
    public Observable<OpenIdConnectDiscoveryProperties> getDefaultWellKnown() {
        return Observable.just(discoveryProperties);
    }

    @Override
    public Observable<OpenIdConnectDiscoveryProperties> getWellKnown(String clientId) {
        return getDefaultWellKnown();
    }
}
