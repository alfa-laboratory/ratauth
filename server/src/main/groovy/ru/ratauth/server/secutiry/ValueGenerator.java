package ru.ratauth.server.secutiry;

public interface ValueGenerator {
    public String generateValue() throws OAuthSystemException;

    public String generateValue(String param) throws OAuthSystemException;
}