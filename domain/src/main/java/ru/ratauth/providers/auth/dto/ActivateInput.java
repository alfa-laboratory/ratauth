package ru.ratauth.providers.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.ratauth.entities.UserInfo;

import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class ActivateInput {

    private Map<String, String> data;
    private Set<String> authContext;
    private UserInfo userInfo;
    private String relyingParty;

}
