package ru.ratauth.server.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceConfiguration {

    private boolean enabled;
    private String url;
    private int readTimeout;
    private String authLogin;
    private String authPassword;
}