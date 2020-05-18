package ru.ratauth.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ratauth.entities.AcrValues;
import ru.ratauth.providers.auth.dto.BaseAuthFields;
import ru.ratauth.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * @author djassan
 * @since 06/11/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthzResponse {
    private String code;
    //intermediate step data for two step authentication
    private Map<String, Object> data;
    private Long expiresIn;
    private String location;
    private String token;
    private TokenType tokenType;
    private String mfaToken;
    private String refreshToken;
    private String sessionToken;
    private String idToken;
    private String redirectURI;
    private AcrValues acrValues;
    //update properties
    private String reason;
    private String updateCode;
    private String updateService;

    private String clientId;
    private Set<String> scopes;

    private String getEncodedRedirectURI() {
        try {
            return URLEncoder.encode(redirectURI, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("This should never happen: UTF-8 encoding is unsupported");
        }
    }

    public String buildURL() {
        StringJoiner joiner = new StringJoiner("&");
        if (!StringUtils.isBlank(redirectURI)) {
            joiner.add("redirect_uri=" + getEncodedRedirectURI());
        }
        if (!StringUtils.isBlank(code)) {
            joiner.add("code=" + code);
        }
        if (expiresIn != null) {
            joiner.add("expires_in=" + expiresIn);
        }
        if (!StringUtils.isBlank(token)) {
            joiner.add("token=" + token);
            joiner.add("token_type=" + tokenType);
        }
        if (!StringUtils.isBlank(refreshToken)) {
            joiner.add("refresh_token=" + refreshToken);
        }
        if (!StringUtils.isBlank(idToken)) {
            joiner.add("id_token=" + idToken);
        }
        if (!StringUtils.isBlank(mfaToken)) {
            joiner.add("mfa_token=" + mfaToken);
        }
        if (StringUtils.isBlank(mfaToken) && !StringUtils.isBlank(sessionToken)) {
            joiner.add("session_token=" + sessionToken);
        }
        if (acrValues != null && acrValues.getValues() != null && !acrValues.getValues().isEmpty()) {
            joiner.add("acr_values=" + acrValues.toString());
        }
        if (clientId != null) {
            joiner.add("client_id=" + clientId);
        }
        if (scopes != null) {
            joiner.add("scope=" + String.join(" ", scopes));
        }
        if (data != null && !data.isEmpty()) {
            data.entrySet().stream()
                    .filter(this::hasValue)
                    .filter(this::isNotUserIdEntry)
                    .forEach(entry -> joiner.add(entry.getKey() + "=" + entry.getValue().toString()));
        }
        if (!StringUtils.isBlank(reason) && !StringUtils.isBlank(updateCode) && !StringUtils.isBlank(updateService)) {
            joiner.add("reason=" + reason)
                    .add("update_code=" + updateCode)
                    .add("update_service=" + updateService);
        }
        return createRedirectURI(location, joiner.toString());
    }

    private boolean hasValue(Map.Entry<String, Object> e) {
        return e.getValue() != null;
    }

    private boolean isNotUserIdEntry(Map.Entry<String, Object> e) {
        return !Objects.equals(BaseAuthFields.USER_ID.val(), e.getKey());
    }

    private static String createRedirectURI(String url, String parameter) {
        if (parameter == null || parameter.isEmpty()) {
            return url;
        }
        if (url == null) {
            url = "";
        }
        StringBuilder sb = new StringBuilder(url);
        if (url.contains("?")) {
            if (!url.endsWith("?") && !url.endsWith("&")) {
                sb.append("&");
            }
        } else {
            sb.append("?");
        }
        return sb.toString() + parameter;
    }

}
