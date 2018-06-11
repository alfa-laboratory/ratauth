package ru.ratauth.server.extended.enroll.activate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ActivateEnrollResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("mfa_token")
    private String mfaToken;
    private Map<String, Object> data;

}
