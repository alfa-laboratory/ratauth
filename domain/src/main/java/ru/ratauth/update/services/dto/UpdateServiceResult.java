package ru.ratauth.update.services.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

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
