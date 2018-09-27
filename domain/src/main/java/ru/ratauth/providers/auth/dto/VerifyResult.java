package ru.ratauth.providers.auth.dto;

import lombok.*;
import ru.ratauth.entities.AcrValues;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyResult {

    @Singular("field")
    private Map<String, Object> data = new HashMap<>();
    private Status status;
    private AcrValues acrValues;
    private boolean required;
    private String reason;

    public enum Status {
        SUCCESS,
        NEED_APPROVAL,
        NEED_UPDATE
    }
}
