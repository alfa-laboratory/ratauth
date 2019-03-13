package ru.ratauth.server.providers

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.http.Status
import ratpack.http.TypedData
import ratpack.http.client.ReceivedResponse
import ru.ratauth.entities.AcrValues
import ru.ratauth.entities.IdentityProvider
import ru.ratauth.exception.ProviderException
import ru.ratauth.providers.auth.dto.ActivateInput
import ru.ratauth.providers.auth.dto.ActivateResult
import ru.ratauth.providers.auth.dto.VerifyInput
import ru.ratauth.providers.auth.dto.VerifyResult
import ru.ratauth.server.command.HystrixIdentityProviderCommand
import ru.ratauth.server.configuration.DestinationConfiguration
import ru.ratauth.server.configuration.IdentityProvidersConfiguration
import ru.ratauth.server.handlers.AuthErrorHandler
import ru.ratauth.server.handlers.HttpClientHolder
import rx.Observable

@Slf4j
@Component
@CompileStatic
class RestIdentityProvider implements IdentityProvider {

    @Autowired
    IdentityProvidersConfiguration identityProvidersConfiguration

    @Autowired
    ObjectMapper jacksonObjectMapper

    String name() {
        return "REST"
    }

    @Override
    Observable<ActivateResult> activate(ActivateInput input) {
        String enroll = input.enroll.first
        DestinationConfiguration config = identityProvidersConfiguration.idp?.get(enroll)?.activate
        Integer timeout = identityProvidersConfiguration.timeout
        log.info("Sending request to ${enroll}")
        return new HystrixIdentityProviderCommand(
                HttpClientHolder.instance,
                input.data,
                input.userInfo,
                input.relyingParty,
                enroll,
                config?.url,
                config?.authLogin,
                config?.authPassword,
                timeout
        )
                .toObservable()
                .map({ ReceivedResponse res ->

            handleErrorIfPresent(res.status, res.body)
            makeActivateResultFromResponse(res)
        })
    }

    private void handleErrorIfPresent(Status status, TypedData body) {
        if (status.'3xx') {
            throw new ProviderException(ProviderException.ID.CONTRACT_VIOLATION, "3xx HTTP code is not allowed")
        }
        if (status.'4xx' || status.'5xx') {
            def e = toExceptionDTO(body.text)
            throw AuthErrorHandler.castToException(e)
        }
    }

    private AuthErrorHandler.ExceptionDTO toExceptionDTO(String body) {
        try {
            return jacksonObjectMapper.readValue(body, AuthErrorHandler.ExceptionDTO)
        } catch (JsonMappingException | JsonParseException e) {
            log.debug("Can't parse exception: '${body}'", e)
            throw new ProviderException(ProviderException.ID.DESERIALIZATION_ERROR, body)
        }
    }

    @Override
    Observable<VerifyResult> verify(VerifyInput input) {
        String enroll = input.enroll.first
        DestinationConfiguration config = identityProvidersConfiguration.idp?.get(enroll)?.verify
        int timeout = identityProvidersConfiguration.timeout

        log.info("Sending request to ${enroll}")
        return new HystrixIdentityProviderCommand(
                HttpClientHolder.instance,
                input.data,
                input.userInfo,
                input.relyingParty,
                enroll,
                config?.url,
                config?.authLogin,
                config?.authPassword,
                timeout
        )
                .toObservable()
                .map({ ReceivedResponse res ->
            handleErrorIfPresent(res.status, res.body)
            makeVerifyResultFromResponse(res)
        })
    }

    private static VerifyResult makeVerifyResultFromResponse(ReceivedResponse receivedResponse) {
        def response = (new JsonSlurper().parseText(receivedResponse.body.text) as List)[0] as Map
        new VerifyResult(
                data: response.data as Map,
                status: VerifyResult.Status.valueOf(response.status as String),
                acrValues: parseAcrValues(response.acrValues as String)
        )
    }

    static AcrValues parseAcrValues(String acrValues) {
        if (!acrValues) {
            return null
        }
        return AcrValues.valueOf(acrValues)
    }

    private static ActivateResult makeActivateResultFromResponse(ReceivedResponse receivedResponse) {
        def response = (new JsonSlurper().parseText(receivedResponse.body.text) as List)[0] as Map
        return new ActivateResult(response.data as Map)
    }
}
