package ru.ratauth.server.providers

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.http.client.ReceivedResponse
import ru.ratauth.entities.AcrValues
import ru.ratauth.entities.IdentityProvider
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.exception.ProviderException
import ru.ratauth.providers.Fields
import ru.ratauth.providers.auth.dto.ActivateInput
import ru.ratauth.providers.auth.dto.ActivateResult
import ru.ratauth.providers.auth.dto.VerifyInput
import ru.ratauth.providers.auth.dto.VerifyResult
import ru.ratauth.server.command.HystrixIdentityProviderCommand
import ru.ratauth.server.configuration.DestinationConfiguration
import ru.ratauth.server.configuration.IdentityProvidersConfiguration
import ru.ratauth.server.handlers.HttpClientHolder
import rx.Observable

@Slf4j
@Component
@CompileStatic
class RestIdentityProvider implements IdentityProvider {

    @Autowired
    IdentityProvidersConfiguration identityProvidersConfiguration

    String name() {
        return "REST"
    }

    @Override
    Observable<ActivateResult> activate(ActivateInput input) {
        String enroll = input.enroll.first
        DestinationConfiguration config = identityProvidersConfiguration.idp?.get(enroll)?.verify

        log.info("Sending request to ${enroll}")
        return new HystrixIdentityProviderCommand(
                HttpClientHolder.instance,
                input.data,
                input.userInfo,
                input.relyingParty,
                enroll,
                config?.url,
                config?.authLogin,
                config?.authPassword
        )
                .toObservable()
                .map({ ReceivedResponse res -> makeActivateResultFromResponse(res)
        })
    }

    @Override
    Observable<VerifyResult> verify(VerifyInput input) {
        String enroll = input.enroll.first
        DestinationConfiguration config = identityProvidersConfiguration.idp?.get(enroll)?.verify

        log.info("Sending request to ${enroll}")
        return new HystrixIdentityProviderCommand(
                HttpClientHolder.instance,
                input.data,
                input.userInfo,
                input.relyingParty,
                enroll,
                config?.url,
                config?.authLogin,
                config?.authPassword
        )
                .toObservable()
                .map({ ReceivedResponse res ->
            if (res.status.'4xx') {
                throw new AuthorizationException(res.body.text)
            }
            if (res.status.'5xx') {
                throw new ProviderException(res.body.text)
            }
            makeVerifyResultFromResponse(res)
        })
    }

    private static VerifyResult makeVerifyResultFromResponse(ReceivedResponse receivedResponse) {
        def response = new JsonSlurper().parseText(receivedResponse.body.text) as Map
        assert response.data[Fields.USER_ID.val()]
        new VerifyResult(
                data:response.data as Map,
                status:VerifyResult.Status.valueOf(response.status as String),
                acrValues:parseAcrValues(response.acrValues as String)
        )
    }

    static AcrValues parseAcrValues(String acrValues) {
        if (!acrValues) {
            return null
        }
        return AcrValues.valueOf(acrValues)
    }

    private static ActivateResult makeActivateResultFromResponse(ReceivedResponse receivedResponse) {
        def response = new JsonSlurper().parseText(receivedResponse.body.text) as Map
        assert response.data[Fields.USER_ID.val()]
        return new ActivateResult(response.data as Map)
    }
}
