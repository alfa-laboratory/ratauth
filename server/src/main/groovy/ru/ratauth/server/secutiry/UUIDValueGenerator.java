package ru.ratauth.server.secutiry;

import java.util.UUID;

public class UUIDValueGenerator implements ValueGenerator {

    @Override
    public String generateValue() throws OAuthSystemException {
        return generateValue(UUID.randomUUID().toString());
    }

    @Override
    public String generateValue(String param) throws OAuthSystemException {
        return UUID.fromString(UUID.nameUUIDFromBytes(param.getBytes()).toString()).toString();
    }
}