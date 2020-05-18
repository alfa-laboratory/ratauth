package ru.ratauth.update.services.dto;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateServiceResult {

    private Status status;
    @Singular("field")
    private Map<String, Object> data = new HashMap<>();

    public enum Status {
        SUCCESS,
        SKIPPED,
        ERROR
    }
}
