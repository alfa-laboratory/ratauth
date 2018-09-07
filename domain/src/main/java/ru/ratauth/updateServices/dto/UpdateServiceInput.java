package ru.ratauth.updateServices.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UpdateServiceInput {

    private String updateService;
    private Map<String, String> data;
    private String relyingParty;
    private String code;
}
