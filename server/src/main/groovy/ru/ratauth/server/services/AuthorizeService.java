package ru.ratauth.server.services;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import ru.ratauth.interaction.TokenResponse;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
public interface AuthorizeService {
  String authenticate(HttpServletRequest request) throws URISyntaxException, OAuthSystemException,OAuthProblemException;
}
