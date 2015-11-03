package ru.ratauth.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import ru.ratauth.interaction.CheckTokenResponse;
import ru.ratauth.interaction.TokenResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author mgorelikov
 * @since 03/11/15
 */
public interface AuthTokenService {
  TokenResponse getToken(HttpServletRequest request) throws OAuthProblemException, OAuthSystemException, JsonProcessingException;
  CheckTokenResponse checkToken(HttpServletRequest request) throws OAuthProblemException, OAuthSystemException;
}
