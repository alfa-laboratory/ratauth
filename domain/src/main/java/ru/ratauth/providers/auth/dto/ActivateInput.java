package ru.ratauth.providers.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Singular;

import java.util.Map;

@Data
@AllArgsConstructor
public class ActivateInput {

    private Map<String, String> data;
    private String relyingParty;

}
