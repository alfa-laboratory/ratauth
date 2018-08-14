package ru.ratauth.services;

import ru.ratauth.entities.UpdateEntry;

public interface UpdateTokenService {

    boolean isValidToken(String token);

    void invalidateToken(String token);

    UpdateEntry createEntry(String sessionId);
}