package ru.ratauth.server.secutiry;

public interface OAuthIssuer {
    public String accessToken() throws OAuthSystemException;

    public String authorizationCode() throws OAuthSystemException;

    public String refreshToken() throws OAuthSystemException;
}