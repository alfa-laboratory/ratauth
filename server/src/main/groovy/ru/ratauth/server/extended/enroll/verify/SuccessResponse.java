package ru.ratauth.server.extended.enroll.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class SuccessResponse extends RedirectResponse {

    private final String code;

    public SuccessResponse(String location, String code) {
        super(location);
        this.code = code;
    }

    @Override
    Map<String, String> getRedirectParameters() {
        return Collections.singletonMap("code", code);
    }

}
