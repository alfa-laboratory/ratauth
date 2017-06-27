package ru.ratauth.providers.auth.dto;

import lombok.*;
import ru.ratauth.entities.UserInfo;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivateResult {

    @Singular("field")
    private Map<String, String> data;
    private UserInfo userInfo;

}
