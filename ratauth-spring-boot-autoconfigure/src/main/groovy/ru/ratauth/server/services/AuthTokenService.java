package ru.ratauth.server.services;

import com.nimbusds.jose.JOSEException;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Session;
import ru.ratauth.interaction.CheckTokenRequest;
import ru.ratauth.interaction.CheckTokenResponse;
import ru.ratauth.interaction.TokenRequest;
import ru.ratauth.interaction.TokenResponse;
import ru.ratauth.server.secutiry.OAuthSystemException;
import rx.Observable;


/**
 * Service for token and check_token endpoints handling
 * @author mgorelikov
 * @since 03/11/15
 */
public interface AuthTokenService {
  Observable<TokenResponse> getToken(TokenRequest oauthRequest) throws OAuthSystemException, JOSEException;

  /**
   * Check access token
   * @return check_token response with JWT token object
   */
  Observable<CheckTokenResponse> checkToken(CheckTokenRequest oauthRequest);

  /**
   * Creates token response object for session corresponding to input relying party
   * @param session
   * @param relyingParty
   * @return token response object with access, refresh and id(jwt) token
   */
  Observable<TokenResponse> createIdTokenAndResponse(Session session, RelyingParty relyingParty);
}
