package ru.ratauth.server.extended.update;

import ru.ratauth.server.extended.common.RedirectResponse;

import java.util.HashMap;
import java.util.Map;

public class UpdateProcessResponse extends RedirectResponse {

    private final String reason;
    private final String updateCode;
    private final String updateService;
    private final Map<String, String> redirectParameters;

    public UpdateProcessResponse(String reason, String updateCode, String updateService, String location) {
        super(location);
        this.reason = reason;
        this.updateCode = updateCode;
        this.updateService = updateService;
        redirectParameters = new HashMap<>();
        redirectParameters.put("update_code", updateCode);
        redirectParameters.put("update_service", updateService);
        redirectParameters.put("reason", reason);
    }

    @Override
    public String putRedirectParameters(String key, String value) {
        return redirectParameters.put(key, value);
    }

    @Override
    public Map<String, String> getRedirectParameters() {
        return redirectParameters;
    }
}
