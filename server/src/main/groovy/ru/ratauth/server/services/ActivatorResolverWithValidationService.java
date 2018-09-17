package ru.ratauth.server.services;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import ru.ratauth.providers.auth.Activator;
import ru.ratauth.providers.auth.ActivatorResolver;
import ru.ratauth.providers.auth.ActivatorWithValidation;
import ru.ratauth.providers.auth.dto.ActivateInput;
import ru.ratauth.providers.auth.dto.ActivateResult;
import rx.Observable;

import java.nio.file.ProviderNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

public class ActivatorResolverWithValidationService implements ActivatorResolver {

    private final Map<String, Activator> activators;
    private final Map<String, ActivatorWithValidation<? super Object>> activatorsWithValidation;

    public ActivatorResolverWithValidationService() {
        this.activators = Collections.emptyMap();
        this.activatorsWithValidation = Collections.emptyMap();
    }

    public ActivatorResolverWithValidationService(List<Activator> activators, List<ActivatorWithValidation<? super Object>> activatorsWithValidation) {
        this.activatorsWithValidation = activatorsWithValidation.stream().collect(toMap(ActivatorWithValidation::name, v -> v));
        this.activators = activators.stream().collect(toMap(Activator::name, v -> v));
    }

    @Override
    public Observable<ActivateResult> activate(String enroll, ActivateInput activateInput) {
        Activator activator = activators.get(enroll);
        if (activator == null) {
            ActivatorWithValidation<? super Object> activatorWithValidation = fetchActivatorWithValidation(enroll);
            Object object = activatorWithValidation.produceInputBean();
            fillWithProperties(object, activateInput.getData());
            // Validation with validation factory will be there
            return activatorWithValidation.activate(object, activateInput);
        }
        return activator.activate(activateInput);
    }

    private void fillWithProperties(Object object, Map<String, String> data) {
        BeanWrapper wrapper = new BeanWrapperImpl(object);
        for (Map.Entry<String, String> property : data.entrySet()) {
            if (wrapper.isWritableProperty(property.getKey())) {
                wrapper.setPropertyValue(property.getKey(), property.getValue());
            }
        }
    }

    private ActivatorWithValidation<? super Object> fetchActivatorWithValidation(String enroll) {
        return ofNullable(activatorsWithValidation.get(enroll))
                .orElseThrow(() -> new ProviderNotFoundException(enroll));
    }
}
