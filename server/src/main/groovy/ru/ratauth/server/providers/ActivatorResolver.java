package ru.ratauth.server.providers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ratauth.providers.auth.Activator;
import ru.ratauth.server.extended.enroll.MissingProviderException;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@Component
public class ActivatorResolver {

    private final Map<String, Activator> activators;

    @Autowired
    public ActivatorResolver(List<Activator> activators) {
        this.activators = activators.stream().collect(toMap(Activator::name, v -> v));
    }

    public Activator find(String enroll) {
        return ofNullable(activators.get(enroll))
                .orElseThrow(() -> new MissingProviderException(enroll));
    }

}
