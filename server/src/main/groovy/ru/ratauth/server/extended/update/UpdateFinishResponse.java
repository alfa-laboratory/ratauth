package ru.ratauth.server.extended.update;

import ru.ratauth.server.extended.common.RedirectResponse;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.valueOf;

public class UpdateFinishResponse extends RedirectResponse {

    private final String code;
    private final long expiresIn;
    private final String sessionToken;
    private final Map<String, String> redirectParameters;

    public UpdateFinishResponse(String location, String sessionToken, String code, long expiresIn) {
        super(location);
        this.code = code;
        this.expiresIn = expiresIn;
        this.sessionToken = sessionToken;
        redirectParameters = new HashMap<>();
        redirectParameters.put("code", code);
        redirectParameters.put("session_token", sessionToken);
        redirectParameters.put("expires_in", valueOf(expiresIn));
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
