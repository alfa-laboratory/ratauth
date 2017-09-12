package ru.ratauth.services;

import ru.ratauth.entities.OpenIdConnectDiscoveryProperties;
import rx.Observable;

/**
 * Created by sserdyuk on 9/11/17.
 */
public interface OpenIdConnectDiscoveryService {

    Observable<OpenIdConnectDiscoveryProperties> getDefaultWellKnown();

    Observable<OpenIdConnectDiscoveryProperties> getWellKnown(String clientId);
}
