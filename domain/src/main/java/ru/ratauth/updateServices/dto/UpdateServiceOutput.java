package ru.ratauth.updateServices.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
public class UpdateServiceOutput {

    private Status status;
    @Singular("field")
    private Map<String, Object> data = new HashMap<>();

    public enum Status {
        SUCCESS,
        SKIPPED,
        ERROR
    }
}
