package ru.ratauth.server.configuration;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DestinationConfiguration {
    String url;
    String authLogin;
    String authPassword;
    Integer attemptMaxValue;
    Integer ttlInSeconds;
}
