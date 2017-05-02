package ru.ratauth.server.handlers

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.server.configuration.OpenIdConnectDiscoveryProperties

import static ratpack.jackson.Jackson.json
/**
 * @author tolkv
 * @version 09/11/2016
 */
@Component
@CompileStatic
class WellKnownHandler implements Action<Chain> {

  @Autowired
  private OpenIdConnectDiscoveryProperties connectDiscoveryProperties

  @Override
  void execute(Chain chain) throws Exception {
    chain.path('.well-known/openid-configuration') { Context ctx ->
      ctx.render(json(
          [
              issuer                                          :connectDiscoveryProperties.issuer,
              authorization_endpoint                          :connectDiscoveryProperties.authorizationEndpoint,
              token_endpoint                                  :connectDiscoveryProperties.tokenEndpoint,
              token_endpoint_auth_signing_alg_values_supported:connectDiscoveryProperties.tokenEndpointAuthSigningAlgValuesSupported,
              registration_endpoint                           :connectDiscoveryProperties.registrationEndpoint,
              userinfo_endpoint                               :connectDiscoveryProperties.userInfoEndpoint,
              check_session_iframe                            :connectDiscoveryProperties.checkSessionIframe,
              end_session_endpoint                            :connectDiscoveryProperties.endSessionEndpoint,
              subject_types_supported                         :connectDiscoveryProperties.subjectTypesSupported,
              response_types_supported                        :connectDiscoveryProperties.responseTypesSupported,
              jwks_uri                                        :connectDiscoveryProperties.jwksUri,
              scopes_supported                                :connectDiscoveryProperties.scopesSupported,
              claims_supported                                :connectDiscoveryProperties.claimsSupported,
          ]
      ))
    }
  }
}
