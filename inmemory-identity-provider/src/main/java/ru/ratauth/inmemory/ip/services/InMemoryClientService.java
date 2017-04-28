package ru.ratauth.inmemory.ip.services;

import ru.ratauth.entities.AuthClient;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.SessionClient;
import ru.ratauth.services.ClientService;
import rx.Observable;

import java.util.List;

import static java.lang.String.*;

public class InMemoryClientService implements ClientService {

    private final List<RelyingParty> relyingParties;
    private final List<AuthClient> authClients;
    private final List<SessionClient> sessionClients;

    public InMemoryClientService(List<RelyingParty> relyingParties, List<AuthClient> authClients, List<SessionClient> sessionClients) {
        this.relyingParties = relyingParties;
        this.authClients = authClients;
        this.sessionClients = sessionClients;
    }

    @Override
    public Observable<AuthClient> getClient(String name) {
        return authClients.stream()
                .filter(authClient -> authClient.getName().equals(name))
                .findFirst()
                .map(Observable::just)
                .orElseGet(() -> Observable.error(new IllegalArgumentException(format("Client with name: %s doesn't exist", name))));
    }

    @Override
    public Observable<RelyingParty> getRelyingParty(String name) {
        return relyingParties.stream()
                .filter(relyingParty -> relyingParty.getName().equals(name))
                .findFirst()
                .map(Observable::just)
                .orElseGet(() -> Observable.error(new IllegalArgumentException(format("Relying party with name: %s doesn't exist", name))));
    }

    @Override
    public Observable<SessionClient> getSessionClient(String name) {
        return sessionClients.stream()
                .filter(sessionClient -> sessionClient.getName().equals(name))
                .findFirst()
                .map(Observable::just)
                .orElseGet(() -> Observable.error(new IllegalArgumentException(format("Session client with name: %s doesn't exist", name))));
    }

}
