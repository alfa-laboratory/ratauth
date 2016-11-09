package ru.ratauth.server.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
/**
 * @author tolkv
 * @version 09/11/2016
 */
@ConfigurationProperties(prefix = 'openid.properties.discovery')
class OpenIdConnectDiscoveryProperties {
  String issuer
  String authorizationEndpoint
  String tokenEndpoint
  List<String> tokenEndpointAuthSigningAlgValuesSupported
  String registrationEndpoint
  String userInfoEndpoint
  String checkSessionIframe
  String endSessionEndpoint
}
