package ru.ratauth.inmemory.ip.services;

import ru.ratauth.entities.AuthClient;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.SessionClient;
import ru.ratauth.services.ClientService;
import rx.Observable;

import java.util.List;

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
            .orElseThrow(() -> new IllegalArgumentException());
  }

  @Override
  public Observable<RelyingParty> getRelyingParty(String name) {
    return relyingParties.stream()
            .filter(relyingParty -> relyingParty.getName().equals(name))
            .findFirst()
            .map(Observable::just)
            .orElseThrow(() -> new IllegalArgumentException());
  }

  @Override
  public Observable<SessionClient> getSessionClient(String name) {
    return sessionClients.stream()
            .filter(sessionClient -> sessionClient.getName().equals(name))
            .findFirst()
            .map(Observable::just)
            .orElseThrow(() -> new IllegalArgumentException());
  }

}
