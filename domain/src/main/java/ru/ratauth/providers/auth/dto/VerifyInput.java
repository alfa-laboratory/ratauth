package ru.ratauth.providers.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.ratauth.entities.AcrValues;
import ru.ratauth.entities.Enroll;
import ru.ratauth.entities.UserInfo;

import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class VerifyInput {

    private Map<String, String> data;
    private Enroll enroll;
    private UserInfo userInfo;
    private String relyingParty;

}
