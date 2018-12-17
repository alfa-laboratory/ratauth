package ru.ratauth.server.services;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
public class HazelcastCachingService {
    private HazelcastInstance hzClient;
    @Value("${hazelcast.auth.login}")
    private String login;
    @Value("${hazelcast.auth.password}")
    private String password;


    @PostConstruct
    private void init() {
        ClientConfig config = new ClientConfig();
        GroupConfig groupConfig = config.getGroupConfig();
        groupConfig.setName(login);
        groupConfig.setPassword(password);
        hzClient = HazelcastClient.newHazelcastClient(config);
    }

    public Map<Object, Object> getMap(String mapName) {
        return hzClient.getMap(mapName);
    }
}
