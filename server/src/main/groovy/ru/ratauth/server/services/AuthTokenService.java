package ru.ratauth.server.services;

import com.nimbusds.jose.JOSEException;
import ru.ratauth.interaction.CheckTokenRequest;
import ru.ratauth.interaction.CheckTokenResponse;
import ru.ratauth.interaction.TokenRequest;
import ru.ratauth.interaction.TokenResponse;
import ru.ratauth.server.secutiry.OAuthSystemException;


/**
 * @author mgorelikov
 * @since 03/11/15
 */
public interface AuthTokenService {
  TokenResponse getToken(TokenRequest oauthRequest) throws OAuthSystemException, JOSEException;
  CheckTokenResponse checkToken(CheckTokenRequest oauthRequest);
}
