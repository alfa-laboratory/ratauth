package ru.ratauth.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateServiceRequest {

    private String code;
    private boolean skip;
    private String clientId;
    private String reason;
    private String updateService;
    private Map<String, String> data;
}
