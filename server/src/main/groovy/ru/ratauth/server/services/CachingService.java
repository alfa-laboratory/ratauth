package ru.ratauth.server.services;

import ru.ratauth.entities.AcrValues;
import ru.ratauth.server.services.dto.CachingUserKey;

public interface CachingService {

    void checkAttemptCount(CachingUserKey countKey, int attempts, int ttl);

    void checkIsAuthAllowed(AcrValues enroll, String userId);

}