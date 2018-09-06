package ru.ratauth.server.configuration;

import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties("ratauth")
public class UpdateServicesConfiguration {
    private Map<String, UpdateServiceConfiguration> updateService;
}