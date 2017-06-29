package ru.ratauth.providers.auth.dto;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyResult {

    @Singular("field")
    private Map<String, String> data = new HashMap<>();
    private Status status;

    public enum Status {
        SUCCESS,
        NEED_APPROVAL
    }
}
