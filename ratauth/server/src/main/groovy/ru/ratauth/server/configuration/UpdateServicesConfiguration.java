package ru.ratauth.server.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("ratauth")
public class UpdateServicesConfiguration {
    private Map<String, UpdateServiceConfiguration> updateServices;
}