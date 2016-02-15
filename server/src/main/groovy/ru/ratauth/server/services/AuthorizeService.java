package ru.ratauth.server.services;

import ru.ratauth.entities.AuthzEntry;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.interaction.AuthzRequest;
import ru.ratauth.interaction.AuthzResponse;
import rx.Observable;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 02/11/15
 */
public interface AuthorizeService {
  Observable<AuthzResponse> authenticate(AuthzRequest request);
  Observable<AuthzEntry> createEntry(RelyingParty relyingParty,Set<String> auds, Set<String> scopes, Map<String, Object> userInfo);
}
