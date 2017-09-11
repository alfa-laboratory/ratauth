package ru.ratauth.server.handlers

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.entities.DiscoveryProperties
import ru.ratauth.server.configuration.OpenIdConnectDiscoveryProperties
import ru.ratauth.services.ClientService
import rx.functions.Action1

import static com.google.common.base.Strings.isNullOrEmpty
import static ratpack.jackson.Jackson.json

/**
 * @author tolkv
 * @version 09/11/2016
 */
@Component
@CompileStatic
@SuppressWarnings(["SpaceAroundMapEntryColon", "ClosureStatementOnOpeningLineOfMultipleLineClosure"])
class WellKnownHandler implements Action<Chain> {

    @Autowired
    private OpenIdConnectDiscoveryProperties openidConf

    @Autowired
    private ClientService clientService

    @Override
    void execute(Chain chain) throws Exception {
        chain.path('.well-known/openid-configuration/:client_id?') { Context ctx ->
            def clientId = ctx.pathTokens.get("client_id")
            if (isNullOrEmpty(clientId)) {
                ctx.render(json(
                    [
                        issuer                                          : openidConf.issuer,
                        authorization_endpoint                          : openidConf.authorizationEndpoint,
                        token_endpoint                                  : openidConf.tokenEndpoint,
                        token_endpoint_auth_signing_alg_values_supported: openidConf.tokenEndpointAuthSigningAlgValuesSupported,
                        registration_endpoint                           : openidConf.registrationEndpoint,
                        userinfo_endpoint                               : openidConf.userInfoEndpoint,
                        check_session_iframe                            : openidConf.checkSessionIframe,
                        end_session_endpoint                            : openidConf.endSessionEndpoint,
                        subject_types_supported                         : openidConf.subjectTypesSupported,
                        response_types_supported                        : openidConf.responseTypesSupported,
                        jwks_uri                                        : openidConf.jwksUri,
                        scopes_supported                                : openidConf.scopesSupported,
                        claims_supported                                : openidConf.claimsSupported,
                    ]
                ))
            } else {
                clientService.getRelyingParty(clientId)
                    .map({ it.discoveryProperties })
                    .map({ DiscoveryProperties props -> json(
                        [
                            issuer                                          : props.issuer,
                            authorization_endpoint                          : props.authorizationEndpoint,
                            token_endpoint                                  : props.tokenEndpoint,
                            token_endpoint_auth_signing_alg_values_supported: props.tokenEndpointAuthSigningAlgValuesSupported,
                            registration_endpoint                           : props.registrationEndpoint,
                            userinfo_endpoint                               : props.userInfoEndpoint,
                            check_session_iframe                            : props.checkSessionIframe,
                            end_session_endpoint                            : props.endSessionEndpoint,
                            subject_types_supported                         : props.subjectTypesSupported,
                            response_types_supported                        : props.responseTypesSupported,
                            jwks_uri                                        : props.jwksUri,
                            scopes_supported                                : props.scopesSupported,
                            claims_supported                                : props.claimsSupported,
                        ]
                    )
                })
                        .subscribe(ctx.&render, errorHandler(ctx))
            }
        }
    }

    static Action1<Throwable> errorHandler(Context ctx) {
        return { Throwable throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable) } as Action1<Throwable>
    }
}