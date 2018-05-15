package ru.ratauth.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 01/11/15
 */

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RelyingParty extends AuthClient {
    private ApplicationType applicationType;
    /**
     * unique name
     */
    private String identityProvider;
    private Long codeTTL;
    private Long tokenTTL;
    private Long refreshTokenTTL;
    private Long sessionTTL;
    /**
     * Set of unique grants (means only internal grants, it's not resource server scope)
     */
    private Set<String> grants;
    private AcrValues defaultAcrValues;
    private Map<String, String> acrUriPaths;

    // URIs
    // array of redirects uris, will be checked in case of custom redirect_uri param in request
    private List<String> redirectURIs;
    // it will be user to redirect end-user in case of redirect_uri was not defined in initial registration request
    private String registrationRedirectURI;
    // it will be user to redirect end-user in case of redirect_uri was not defined in initial authorization request
    private String authorizationRedirectURI;
    private String authorizationPageURI;
    private String registrationPageURI;
    private String incAuthLevelPageURI; // page for increase NIST auth level of end-user
}
