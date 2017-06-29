package ru.ratauth.server.extended.enroll.verify;

import java.util.Collections;
import java.util.Map;

public class SuccessResponse extends RedirectResponse {

    public SuccessResponse(String location) {
        super(location);
    }

    @Override
    Map<String, String> getRedirectParameters() {
        return Collections.emptyMap();
    }

}
