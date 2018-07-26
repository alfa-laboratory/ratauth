package ru.ratauth.server.providers

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.http.client.HttpClient
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
        DestinationConfiguration config = identityProvidersConfiguration.idp?.get(enroll)?.activate

        log.info("data " +input.data + " userInfo " + input.userInfo + " relyingParty " + input.relyingParty +  " enroll " + enroll +" config " + config)
        log.info("Sending activate request to ${enroll}")
        log.info("HUITA" + HttpClientHolder.instance)
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
            log.info("YA VNUTRY BLOKA AZAZAZA      _______ " + res.body.text)
            makeActivateResultFromResponse(res)
        })
    }

    @Override
    Observable<VerifyResult> verify(VerifyInput input) {
        String enroll = input.enroll.first
        DestinationConfiguration config = identityProvidersConfiguration.idp?.get(enroll)?.verify
        log.info("data " +input.data + " userInfo " + input.userInfo + " relyingParty " + input.relyingParty +  " enroll " + enroll)
        log.info("Sending verify request to ${enroll}")
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
        log.info("TRATATATATTA " + receivedResponse.body.text)
        def response = (new JsonSlurper().parseText(receivedResponse.body.text)  as List)[0] as Map
        log.info("RESPOOOOOONSE " + response + "\nDATAAAAAAA" + response.data)
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
        log.info("___________________________\n " + receivedResponse.body.text + "\n_____________")
        def response = new JsonSlurper().parseText(receivedResponse.body.text) as Map
        assert response.data[Fields.USER_ID.val()]
        return new ActivateResult(response.data as Map)
    }
}
