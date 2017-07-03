package ru.ratauth.server.extended.enroll.verify;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class VerifyEnrollRequest {
    private String mfaToken;
    private Map<String, String> data;
    private String clientId;
    private Set<String> scope;
    private Set<String> authContext;
    private Set<String> enroll;

    private String redirectURI;
}
