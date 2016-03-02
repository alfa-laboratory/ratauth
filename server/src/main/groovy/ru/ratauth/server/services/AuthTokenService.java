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
 * @author mgorelikov
 * @since 03/11/15
 */
public interface AuthTokenService {
  Observable<TokenResponse> getToken(TokenRequest oauthRequest) throws OAuthSystemException, JOSEException;

  Observable<CheckTokenResponse> checkToken(CheckTokenRequest oauthRequest);

  Observable<TokenResponse> createIdTokenAndResponse(Session session, RelyingParty relyingParty);
}
