package ru.ratauth.server.updateServices;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import ru.ratauth.updateServices.UpdateService;

import static java.util.stream.Collectors.toMap;

@Component
public class UpdateServiceResolverImpl implements UpdateServiceResolver {

    private final Map<String, UpdateService> updateServices;

    public UpdateServiceResolverImpl(List<UpdateService> updateServices) {
        this.updateServices = updateServices.stream().collect(toMap(UpdateService::name, v -> v));
    }

    @Override
    public UpdateService getUpdateService(String updateProviderName) {
        return this.updateServices.get(updateProviderName);
    }
}
