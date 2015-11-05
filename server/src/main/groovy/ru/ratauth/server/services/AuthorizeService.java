package ru.ratauth.server.services;

import ru.ratauth.interaction.AuthzRequest;
import ru.ratauth.interaction.AuthzResponse;
import ru.ratauth.server.secutiry.OAuthSystemException;

import java.net.URISyntaxException;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
public interface AuthorizeService {
  AuthzResponse authenticate(AuthzRequest request) throws URISyntaxException, OAuthSystemException;
}
