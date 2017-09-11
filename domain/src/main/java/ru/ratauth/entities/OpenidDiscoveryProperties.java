package ru.ratauth.entities;

import lombok.Data;

import java.util.List;

/**
 * Created by sserdyuk on 9/8/17.
 */
@Data
public class OpenidDiscoveryProperties implements DiscoveryProperties {
    // Required
    private String issuer;
    private String jwksUri;
    private List<String> responseTypesSupported;
    private List<String> subjectTypesSupported;
    private List<String> tokenEndpointAuthSigningAlgValuesSupported;

    // Optional
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String checkSessionIframe;
    private String endSessionEndpoint;

    // Recommended
    private String userInfoEndpoint;
    private String registrationEndpoint;
    private List<String> scopesSupported;
    private List<String> claimsSupported;
}
