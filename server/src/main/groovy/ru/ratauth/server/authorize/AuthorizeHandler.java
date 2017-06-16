package ru.ratauth.server.authorize;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.http.Request;
import ru.ratauth.server.configuration.OpenIdConnectDiscoveryProperties;

import static java.lang.String.format;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthorizeHandler implements Action<Chain> {

    @Autowired
    private final OpenIdConnectDiscoveryProperties openIdConnectDiscoveryProperties;

    @Override
    public void execute(Chain chain) throws Exception {
        chain.get("auth", context -> {
            Request request = context.getRequest();
            String acr = request.getQueryParams().get("acr");
            String clientId = request.getQueryParams().get("client_id");

            if (acr == null) {
                context.error(new IllegalArgumentException("acr can not be null"));
            }

            if (clientId == null) {
                context.error(new IllegalArgumentException("client_id can not be null"));
            }

            context.redirect(format("%s/%s?%s",
                    openIdConnectDiscoveryProperties.getAuthorizationEndpoint(),
                    acr,
                    request.getQuery()));
        });
    }

}
