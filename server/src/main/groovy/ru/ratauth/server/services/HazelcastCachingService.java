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
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class HazelcastCachingService implements CachingService {

    private HazelcastInstance hazelcastInstance;
//    @Value("${hazelcast.auth.login:}")
//    private String login;
//    @Value("${hazelcast.auth.password:}")
//    private String password;

    @Value("hazelcast.nodes:")
    private List<String> host;

    @PostConstruct
    public void init() {
        log.error("HAZELCAST HOSTS " + host.toString());
        ClientNetworkConfig networkConfig = new ClientNetworkConfig().setSmartRouting(true)
                .setAddresses(Arrays.asList("asappdev1:5702"))
                .setRedoOperation(true)
                .setConnectionTimeout(5000)
                .setConnectionAttemptLimit(5);


        ClientConfig config = new ClientConfig();
        GroupConfig groupConfig = config.getGroupConfig();
        groupConfig.setName("test");
        groupConfig.setPassword("test1");
        config.setNetworkConfig(networkConfig);
        hazelcastInstance = HazelcastClient.newHazelcastClient(config);

    }

    public IMap<Object, Object> getMap(String mapName) {
        return hazelcastInstance.getMap(mapName);
    }

    public HazelcastInstance getInstance() {
        return hazelcastInstance;
    }
}
