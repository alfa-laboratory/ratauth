package ru.ratauth.server.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ratauth.entities.AcrValues;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceConfiguration {

    private String uri;
    private int readTimeout;
    private String authLogin;
    private String authPassword;
    private String allowedAcrValues;
}