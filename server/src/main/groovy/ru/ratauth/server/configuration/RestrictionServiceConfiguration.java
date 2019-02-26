package ru.ratauth.server.configuration;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@NoArgsConstructor
@ConfigurationProperties("ratauth.hazelcast")
public class RestrictionServiceConfiguration {
    private String name;
    private String password;
    private List<String> nodes;
}

