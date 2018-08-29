package ru.ratauth.server.updateServices;

import ru.ratauth.updateServices.UpdateService;

public interface UpdateServiceResolver {

    UpdateService getUpdateProvider(String updateProviderName);
}
