package ru.ratauth.server.extended.enroll.verify;

import ru.ratauth.server.extended.common.RedirectResponse;

import java.util.HashMap;
import java.util.Map;

public class SuccessResponse extends RedirectResponse {

    private final String code;
    private final Map<String, String> redirectParameters;

    public SuccessResponse(String location, String code, long expiresIn) {
        super(location);
        redirectParameters = new HashMap<>();
        redirectParameters.put("code", code);
        redirectParameters.put("expires_in", String.valueOf(expiresIn));
        this.code = code;
    }

    public String putRedirectParameters(String key, String value) {
        return redirectParameters.put(key, value);
    }

    @Override
    public Map<String, String> getRedirectParameters() {
        return redirectParameters;
    }
}
