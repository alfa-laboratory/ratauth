package ru.ratauth.providers.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ratauth.entities.Enroll;
import ru.ratauth.entities.UserInfo;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyInput {

    private Map<String, String> data;
    private Enroll enroll;
    private UserInfo userInfo;
    private String relyingParty;

}
