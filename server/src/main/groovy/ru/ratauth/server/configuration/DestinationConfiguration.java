package ru.ratauth.server.configuration;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class DestinationConfiguration {
    String url;
    String authLogin;
    String authPassword;
    Integer attemptMaxValue;
    Integer ttlInSeconds;
    List<String> clientId;
}
