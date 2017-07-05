package ru.ratauth.server.extended.enroll.activate;

import lombok.Data;
import ru.ratauth.entities.AcrValue;
import ru.ratauth.entities.Enroll;

import java.util.Map;
import java.util.Set;

@Data
public class ActivateEnrollRequest {
    private String mfaToken;
    private Map<String, String> data;
    private String clientId;
    private Set<String> scope;
    private AcrValue authContext;
    private Enroll enroll;
}
