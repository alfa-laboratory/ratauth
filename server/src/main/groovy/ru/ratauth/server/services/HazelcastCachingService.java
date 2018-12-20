package ru.ratauth.server.services;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
public class HazelcastCachingService implements CachingService {

    private HazelcastInstance hazelcastInstance;
    @Value("${ratauth.hazelcast.auth.name:}")
    private String name;
    @Value("${ratauth.hazelcast.auth.password:}")
    private String password;

    @Value("${ratauth.hazelcast.nodes:}")
    private List<String> host;

    private String ATTEMPT_COUNT_MAP_NAME = "attemptCacheCount";

    @PostConstruct
    public void init() {
        ClientNetworkConfig networkConfig = new ClientNetworkConfig().setSmartRouting(true)
                .setAddresses(host)
                .setRedoOperation(true)
                .setConnectionTimeout(5000)
                .setConnectionAttemptLimit(5);

        ClientConfig config = new ClientConfig();
        GroupConfig groupConfig = config.getGroupConfig();
        groupConfig.setName(name);
        groupConfig.setPassword(password);
        config.setNetworkConfig(networkConfig);
        hazelcastInstance = HazelcastClient.newHazelcastClient(config);

    }

    public IMap<Object, Object> getMap(){
        return hazelcastInstance.getMap(ATTEMPT_COUNT_MAP_NAME);
    }

}
