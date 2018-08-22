package ru.ratauth.server.extended.update;

import java.util.HashMap;
import java.util.Map;
import ru.ratauth.server.extended.common.RedirectResponse;

public class UpdateResponse extends RedirectResponse {

    private final String reason;
    private final String updateCode;
    private final Map<String, String> redirectParameters;

    public UpdateResponse(String reason, String updateCode, String location) {
        super(location);
        this.reason = reason;
        this.updateCode = updateCode;
        redirectParameters = new HashMap<>();
        redirectParameters.put("update_code", updateCode);
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
