package ru.ratauth.services;

import ru.ratauth.entities.AcrValues;
import ru.ratauth.entities.RestrictionUserKey;

public interface RestrictionService {

    void checkAttemptCount(RestrictionUserKey countKey, int attempts, int ttl);

    void checkIsAuthAllowed(String clientId, String userId, AcrValues enroll,  int maxAttempts, int maxAttemptsTTL);

}