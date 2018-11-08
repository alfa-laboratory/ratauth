package ru.ratauth.providers.auth.dto;

import lombok.*;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 26/01/16
 * Registration/Authentication input object.
 * Still different provider could require different fields during auth/registration process and could have multiphase flow - so we have abstract map of objects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthInput {
    @Singular("field")
    private Map<String, String> data;
    private String relyingParty;
}
