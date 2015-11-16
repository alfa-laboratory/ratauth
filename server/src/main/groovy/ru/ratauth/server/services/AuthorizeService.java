package ru.ratauth.server.services;

import ru.ratauth.interaction.AuthzRequest;
import ru.ratauth.interaction.AuthzResponse;
import rx.Observable;

import java.net.URISyntaxException;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
public interface AuthorizeService {
  Observable<AuthzResponse> authenticate(AuthzRequest request);
}
