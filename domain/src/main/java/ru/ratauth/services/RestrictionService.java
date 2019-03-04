package ru.ratauth.services;

import ru.ratauth.entities.AcrValues;

public interface RestrictionService {

    void checkIsAuthAllowed(String clientId, String userId, AcrValues enroll, int maxAttempts, int maxAttemptsTTL);

}