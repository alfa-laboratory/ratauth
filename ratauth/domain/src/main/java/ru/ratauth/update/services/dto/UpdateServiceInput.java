package ru.ratauth.update.services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class UpdateServiceInput {
    private String updateService;
    private Map<String, String> data;
    private String relyingParty;
    private String code;
    private Boolean skip;
}
