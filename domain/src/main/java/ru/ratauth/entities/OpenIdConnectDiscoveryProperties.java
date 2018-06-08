package ru.ratauth.entities;

import java.util.List;

/**
 * Created by sserdyuk on 9/11/17.
 */
public interface OpenIdConnectDiscoveryProperties {
    String getIssuer();

    String getJwksUri();

    List<String> getResponseTypesSupported();

    List<String> getSubjectTypesSupported();

    List<String> getTokenEndpointAuthSigningAlgValuesSupported();

    String getAuthorizationEndpoint();

    String getTokenEndpoint();

    String getCheckTokenEndpoint();

    String getCheckSessionIframe();

    String getEndSessionEndpoint();

    String getUserInfoEndpoint();

    String getAfpEndpoint();

    String getRegistrationEndpoint();

    List<String> getScopesSupported();

    List<String> getClaimsSupported();
}
