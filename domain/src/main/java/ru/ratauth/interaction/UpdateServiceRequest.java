package ru.ratauth.interaction;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateServiceRequest {

    private String code;
    private boolean skip;
    private String clientId;
    private String updateService;
    private Map<String,String> data;
}
