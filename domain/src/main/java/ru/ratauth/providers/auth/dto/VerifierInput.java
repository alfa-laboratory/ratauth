package ru.ratauth.providers.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class VerifierInput {

    private Map<String, String> data;
    private String relyingParty;

}