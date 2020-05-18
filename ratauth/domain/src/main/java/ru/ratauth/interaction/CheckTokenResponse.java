package ru.ratauth.interaction;

import lombok.*;

import java.util.Set;

/**
 * @author mgorelikov
 * @since 03/11/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckTokenResponse {
    private String idToken;
    private Long expiresIn;
    private String clientId;
    private @Singular
    Set<String> scopes;
    private Boolean active;
}
