package ru.ratauth.server.configuration;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("ratauth")
public class UpdateServicesConfiguration {
    private Map<String, UpdateServiceConfiguration> updateServices;
}