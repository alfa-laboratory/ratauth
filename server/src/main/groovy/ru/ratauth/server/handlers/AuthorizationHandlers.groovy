package ru.ratauth.server.handlers

import io.netty.handler.codec.http.HttpResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.form.Form
import ratpack.func.Action
import ratpack.func.Block
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.entities.AcrValues
import ru.ratauth.interaction.AuthzRequest
import ru.ratauth.server.acr.AcrResolver
import ru.ratauth.server.services.AuthClientService
import ru.ratauth.server.services.AuthorizeService
import ru.ratauth.server.utils.AuthorizeHandlerValidator
import rx.Observable

import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.interaction.GrantType.AUTHENTICATION_TOKEN
import static ru.ratauth.interaction.GrantType.SESSION_TOKEN
import static ru.ratauth.server.handlers.readers.AuthzRequestReader.readAuthzRequest
import static ru.ratauth.server.handlers.readers.AuthzRequestReader.readClientId
import static ru.ratauth.utils.URIUtils.appendQuery

/**
 * @author mgorelikov
 * @since 30/10/15
 */
@Component
class AuthorizationHandlers implements Action<Chain> {

    @Autowired
    private AuthorizeHandlerValidator authorizeHandlerValidator;

    @Autowired
    private AuthClientService authClientService

    @Autowired
    private AuthorizeService authorizeService

    @Autowired
    private AcrResolver acrResolver

    @Override
    void execute(Chain chain) throws Exception {
        chain.path('authorize') { ctx ->
            ctx.byMethod { meth ->
                meth.get(new Block() {
                    @Override
                    void execute() throws Exception {
                        ctx.byContent { cont ->
                            cont.html(new Block() {
                                @Override
                                void execute() throws Exception {
                                    redirectToWeb(ctx)
                                }
                            })
                            cont.noMatch(new Block() {
                                @Override
                                void execute() throws Exception {
                                    def requestObs = Observable.just(readAuthzRequest(ctx.request.queryParams, ctx.request.headers))
                                    requestObs.bindExec()
                                    authorize(ctx, requestObs)
                                }
                            })
                        }
                    }
                })
                meth.post(new Block() {
                    @Override
                    void execute() throws Exception {
                        def queryParams = ctx.parse(Form)
                        def requestObs = observe(queryParams).map { res -> readAuthzRequest(res, ctx.request.headers) }
                        authorize(ctx, requestObs)
                    }
                })
            }
        }
    }

    private void redirectToWeb(Context context) {

        def clientId = readClientId(context.request.queryParams)

        String acr = resolveAcr(context)
        String acrUriPath = resolveAcrPath(clientId, acr)

        def pageURIObs = authClientService.getAuthorizationPageURI(clientId, context.request.query)

        pageURIObs.map({ url -> new URL(url) })
                .map({ url -> url.path = "$url.path/$acrUriPath"; url })
                .map({ url -> appendAcrValues(url, clientId) })
                .map({ url -> url.toString() })
                .bindExec()
                .subscribe {
                    res -> context.redirect(HttpResponseStatus.MOVED_PERMANENTLY.code(), res)
                }
    }

    private URL appendAcrValues(URL url, String clientId) {
        if (!url.query?.contains("acr_values=")) {
            AcrValues defaultAcrValues = authClientService.loadRelyingParty(clientId)
                    .toBlocking()
                    .single()
                    .defaultAcrValues

            return new URL(appendQuery(url.toString(), "acr_values=" + defaultAcrValues.toString()))
        }
        return url
    }

    private String resolveAcrPath(String clientId, String acr) {
        authClientService.loadRelyingParty(clientId)
                .map { relyingParty -> relyingParty.acrUriPaths?.get(acr) }
                .filter { path -> path != null }
                .defaultIfEmpty(acr)
                .toBlocking()
                .single()
    }

    private String resolveAcr(Context context) {
        Optional.of(context)
                .filter({ authorizeHandlerValidator.validate(it) })
                .map({ it.request })
                .map({ acrResolver.resolve(it) })
                .map({ acrMatcher -> acrMatcher.match(context.request) })
                .get()
    }

    private void authorize(Context ctx, Observable<AuthzRequest> requestObs) {
        requestObs.flatMap { authRequest ->
            if (AUTHENTICATION_TOKEN == authRequest.grantType || SESSION_TOKEN == authRequest.grantType) {
                authorizeService.crossAuthenticate(authRequest)
            } else {
                authorizeService.authenticate(authRequest)
            }
        } subscribe({
            res -> ctx.redirect(HttpResponseStatus.FOUND.code(), res.buildURL())
        }, { /*on error*/
        throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable)
        }
        )

    }
}
