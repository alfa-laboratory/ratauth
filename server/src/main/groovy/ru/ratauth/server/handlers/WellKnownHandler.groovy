package ru.ratauth.server.handlers

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.server.services.log.ResponseLogger
import ru.ratauth.services.OpenIdConnectDiscoveryService
import rx.Observable
import rx.functions.Action1

import static ratpack.jackson.Jackson.json

/**
 * @author tolkv
 * @version 09/11/2016
 */
@Component
@CompileStatic
@SuppressWarnings("SpaceAroundMapEntryColon")
class WellKnownHandler implements Action<Chain> {

    @Autowired
    private OpenIdConnectDiscoveryService discoveryService
    @Autowired
    private ResponseLogger responseLogger

    @Override
    void execute(Chain chain) throws Exception {
        chain.path('.well-known/openid-configuration/:client_id?') { Context ctx ->
            def clientId = ctx.pathTokens["client_id"]
            Observable.just(new Object()).map {
                def response = "1234"
//                        [
//                                issuer                                          : it.issuer,
//                                authorization_endpoint                          : it.authorizationEndpoint,
//                                token_endpoint                                  : it.tokenEndpoint,
//                                token_endpoint_auth_signing_alg_values_supported: it.tokenEndpointAuthSigningAlgValuesSupported,
//                                registration_endpoint                           : it.registrationEndpoint,
//                                userinfo_endpoint                               : it.userInfoEndpoint,
//                                check_session_iframe                            : it.checkSessionIframe,
//                                end_session_endpoint                            : it.endSessionEndpoint,
//                                subject_types_supported                         : it.subjectTypesSupported,
//                                response_types_supported                        : it.responseTypesSupported,
//                                jwks_uri                                        : it.jwksUri,
//                                scopes_supported                                : it.scopesSupported,
//                                claims_supported                                : it.claimsSupported,
//                        ]
                responseLogger.logResponse response
                json(response)
            }.subscribe(ctx.&render, errorHandler(ctx))
        }
    }

    static Action1<Throwable> errorHandler(Context ctx) {
        return { Throwable throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable) } as Action1<Throwable>
    }
}