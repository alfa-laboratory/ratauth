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
import ru.ratauth.server.configuration.HazelcastServiceConfiguration;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HazelcastCachingService implements CachingService {

    private HazelcastInstance hazelcastInstance;
    private final HazelcastServiceConfiguration hazelcastServiceConfiguration;
    private String ATTEMPT_COUNT_MAP_NAME = "attemptCacheCount";


    private void init() {
        ClientNetworkConfig networkConfig = new ClientNetworkConfig().setSmartRouting(true)
                .setAddresses(hazelcastServiceConfiguration.getNodes())
                .setRedoOperation(true)
                .setConnectionTimeout(5000)
                .setConnectionAttemptLimit(5);

        ClientConfig config = new ClientConfig();
        GroupConfig groupConfig = config.getGroupConfig();
        groupConfig.setName(hazelcastServiceConfiguration.getName());
        groupConfig.setPassword(hazelcastServiceConfiguration.getPassword());
        config.setNetworkConfig(networkConfig);
        hazelcastInstance = HazelcastClient.newHazelcastClient(config);

    }

    public IMap<Object, Object> getMap() {

        if (hazelcastInstance == null) {
            init();
        }

        return hazelcastInstance.getMap(ATTEMPT_COUNT_MAP_NAME);
    }

}
