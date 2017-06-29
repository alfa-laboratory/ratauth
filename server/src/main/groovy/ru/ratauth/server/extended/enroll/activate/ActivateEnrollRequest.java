package ru.ratauth.server.extended.enroll.activate;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class ActivateEnrollRequest {
    private String mfaToken;
    private Map<String, String> data;
    private String clientId;
    private Set<String> scope;
    private Set<String> authContext;
    private Set<String> enroll;
}
