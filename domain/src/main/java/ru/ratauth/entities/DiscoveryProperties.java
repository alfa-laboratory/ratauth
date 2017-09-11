package ru.ratauth.entities;

import java.util.List;

/**
 * Created by sserdyuk on 9/8/17.
 */
public interface DiscoveryProperties {

    String getIssuer();

    String getJwksUri();

    List<String> getResponseTypesSupported();

    List<String> getSubjectTypesSupported();

    List<String> getTokenEndpointAuthSigningAlgValuesSupported();

    String getAuthorizationEndpoint();

    String getTokenEndpoint();

    String getCheckSessionIframe();

    String getEndSessionEndpoint();

    String getUserInfoEndpoint();

    String getRegistrationEndpoint();

    List<String> getScopesSupported();

    List<String> getClaimsSupported();
}
