package ru.ratauth.server.secutiry;

public interface OAuthIssuer {

    String accessToken();

    String authorizationCode();

    String refreshToken();
}