package ru.ratauth.server.handlers

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.server.providers.ActivatorResolver
import ru.ratauth.server.providers.VerifierResolver

import static ratpack.jackson.Jackson.json

@Slf4j
@Component
@CompileStatic
class ActivatorsVerifiersVersionHandler implements Action<Chain> {

    @Autowired
    ActivatorResolver activatorResolver;

    @Autowired
    VerifierResolver verifierResolver;

    @Override
    void execute(Chain chain) {
        chain
                .get('providers/:clientId/activator/version') { Context ctx ->
            ctx.render(json(version: activatorResolver.find(ctx.pathTokens["clientId"]).version()))
        }       .get('providers/:clientId/verifier/version') { Context ctx ->
            ctx.render(json(version: verifierResolver.find(ctx.pathTokens["clientId"]).version()))
        }
    }

}
