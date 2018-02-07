package ru.ratauth.server.extended.enroll.verify;

import lombok.Data;
import ru.ratauth.entities.AcrValues;

import java.util.Map;
import java.util.Set;

@Data
public class VerifyEnrollRequest {
    private String mfaToken;
    private String clientId;
    private Set<String> scope;
    private AcrValues authContext;
    private AcrValues enroll;
    private String redirectURI;
    private String state;

    private Map<String, String> data;
}