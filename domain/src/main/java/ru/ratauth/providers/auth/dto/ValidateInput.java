package ru.ratauth.providers.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ValidateInput {

    private Map<String, String> data;
    private String relyingParty;

}
