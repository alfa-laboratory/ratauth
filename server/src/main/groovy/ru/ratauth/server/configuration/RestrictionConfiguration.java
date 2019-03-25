package ru.ratauth.server.configuration;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Data
@RequiredArgsConstructor
public class RestrictionConfiguration {
    Integer attemptMaxValue;
    Integer ttlInMinutes;
    List<String> clientId;
}
