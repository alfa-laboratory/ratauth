package ru.ratauth.providers.registrations.dto;

import lombok.*;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 28/01/16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegInput {
    @Singular("data")
    private Map<String, String> data;
    private String relyingParty;
}
