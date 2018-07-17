package ru.ratauth.providers.auth;

import lombok.extern.slf4j.Slf4j;
import ru.ratauth.exception.MissingProviderException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

public class ActivatorResolver {

    private final Map<String, Activator> activators;

    public ActivatorResolver() {
        this.activators = new HashMap<>();
    }

    public ActivatorResolver(List<Activator> activators) {
        this.activators = activators.stream().collect(toMap(Activator::name, v -> v));
    }

    public Activator find(String enroll) {
        return ofNullable(activators.get(enroll))
                .orElseThrow(() -> new MissingProviderException(enroll));
    }

}
