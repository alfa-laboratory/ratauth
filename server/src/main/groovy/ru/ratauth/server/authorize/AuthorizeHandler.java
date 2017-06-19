package ru.ratauth.server.authorize;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.handling.Context;
import ratpack.http.Request;
import ru.ratauth.server.acr.AcrResolver;
import ru.ratauth.server.configuration.OpenIdConnectDiscoveryProperties;

import static java.lang.String.format;
import static javaslang.control.Option.of;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthorizeHandler implements Action<Chain> {

    private final OpenIdConnectDiscoveryProperties openIdConnectDiscoveryProperties;
    private final AcrResolver acrResolver;
    private final AuthorizeHandlerValidator authorizeHandlerValidator;


    @Override
    public void execute(Chain chain) throws Exception {
        chain.get("auth", context -> {
            Request request = context.getRequest();

            String acr = of(context)
                    .filter(authorizeHandlerValidator::validate)
                    .map(Context::getRequest)
                    .map(acrResolver::resolve)
                    .map(acrMatcher -> acrMatcher.match(request))
                    .get();

            context.redirect(format("%s/%s?%s",
                    openIdConnectDiscoveryProperties.getAuthorizationEndpoint(),
                    acr,
                    request.getQuery()));
        });
    }

}
