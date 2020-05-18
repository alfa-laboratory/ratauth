package ru.ratauth.server.secutiry;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
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
