package ru.ratauth.server.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.alfabank.api.ObservableCommandFactory;
import ru.alfabank.api.rx.auth.methods.AuthRefByCardRequest;
import ru.alfabank.api.rx.auth.methods.AuthRefByCardResponse;
import ru.alfabank.ws.cs.wscommontypes10.WSCommonParms;
import ru.alfalab.cxf.starter.configuration.WSConfiguration;
import rx.Observable;

/**
 * Created by sserdyuk on 7/4/17.
 */
@Service
@AllArgsConstructor
public class ReferenceService {

    private final ObservableCommandFactory<AuthRefByCardResponse, AuthRefByCardRequest> authRefByCardCommandFactory;
    private final WSConfiguration wsConfiguration;

    public Observable<AuthRefByCardResponse> create(String cus, String relyingParty) {

        WSCommonParms wsCommonParms = wsConfiguration.getParams(relyingParty);
        String channel = wsConfiguration.getXm(relyingParty);
        int timeout = wsConfiguration.getTimeout(relyingParty);

        return authRefByCardCommandFactory.getCommand(
                wsCommonParms,
                AuthRefByCardRequest.builder()
                        .cus(cus)
                        .xm(channel)
                        .operationType(AuthRefByCardRequest.OperationType.AUTH)
                        .build(),
                timeout)
                .toObservable();
    }
}
