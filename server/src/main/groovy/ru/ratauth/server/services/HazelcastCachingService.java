package ru.ratauth.server.services;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.AcrValues;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.server.configuration.DestinationConfiguration;
import ru.ratauth.server.configuration.HazelcastServiceConfiguration;
import ru.ratauth.server.configuration.IdentityProvidersConfiguration;
import ru.ratauth.server.services.dto.CachingUserKey;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HazelcastCachingService implements CachingService {

    private HazelcastInstance hazelcastInstance;
    private final HazelcastServiceConfiguration hazelcastServiceConfiguration;
    private String ATTEMPT_COUNT_MAP_NAME = "attemptCacheCount";
    private final IdentityProvidersConfiguration identityProvidersConfiguration;

    public void checkAttemptCount(CachingUserKey countKey, int maxAttempts, int maxAttemptsTTL) {
        IMap<CachingUserKey, Integer> attemptCacheCount = getInstance().getMap(ATTEMPT_COUNT_MAP_NAME);
        int countValue = attemptCacheCount.get(countKey) == null ? 0 : attemptCacheCount.get(countKey);

        if (countValue < maxAttempts) {
            log.debug("Increment attempt count for user " + countKey.getUserId() + " with enroll " + countKey.getAcrValue());
            if (countValue == 0)
                attemptCacheCount.put(countKey, ++countValue, maxAttemptsTTL, TimeUnit.MINUTES);
            else {
                long ttl = attemptCacheCount.getEntryView(countKey).getExpirationTime() - new Date().getTime();
                attemptCacheCount.put(countKey, ++countValue, ttl, TimeUnit.MILLISECONDS);
            }

            log.debug("Attempt count for user " + countKey.getUserId() + " is " + countKey.toString());

        } else {
            throw new AuthorizationException(AuthorizationException.ID.TOO_MANY_ATTEMPTS.getBaseText(),"User with id " + countKey.getUserId() + " is not allowed to authorize using " + countKey.getAcrValue() + " for some time ");
        }
    }

    public void checkIsAuthAllowed(AcrValues enroll, String userId) {
        log.info("Checking if auth is allowed for user " + userId + " with enroll " + enroll);
        CachingUserKey countKey = new CachingUserKey(userId, enroll.getFirst());
        DestinationConfiguration destinationConfiguration = identityProvidersConfiguration.getIdp().get(enroll.getFirst()).getRestrictions();
        if(destinationConfiguration != null) {
            int maxAttempts = destinationConfiguration.getAttemptMaxValue();
            int maxAttemptsTTL = destinationConfiguration.getTtlInSeconds();
            checkAttemptCount(countKey, maxAttempts, maxAttemptsTTL);
        }

    }

    private ClientConfig configure() {
        ClientConfig config = new ClientConfig();
        config.getGroupConfig()
                .setName(hazelcastServiceConfiguration.getName())
                .setPassword(hazelcastServiceConfiguration.getPassword());
        config.setNetworkConfig(
                new ClientNetworkConfig()
                        .setSmartRouting(true)
                        .setAddresses(hazelcastServiceConfiguration.getNodes())
                        .setRedoOperation(true)
                        .setConnectionTimeout(5000)
                        .setConnectionAttemptLimit(5)
        );
        return config;
    }

    private HazelcastInstance getInstance() {
        if (hazelcastInstance == null) {
            hazelcastInstance = HazelcastClient.newHazelcastClient(configure());
        }
        return hazelcastInstance;
    }
}
