package ru.ratauth.providers.auth.dto;

import lombok.*;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifierResult {

    @Singular("field")
    private Map<String, String> data;
    private Status status;

    public enum Status {
        SUCCESS,
        NEED_APPROVAL
    }
}
