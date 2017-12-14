package ru.ratauth.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ratauth.entities.AcrValues;
import ru.ratauth.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
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
            joiner.add("refresh_token=" + refreshToken.toString());
        }
        if (!StringUtils.isBlank(idToken)) {
            joiner.add("id_token=" + idToken.toString());
        }
        if (!StringUtils.isBlank(mfaToken)) {
            joiner.add("mfa_token=" + mfaToken.toString());
        }
        if (StringUtils.isBlank(mfaToken) && !StringUtils.isBlank(sessionToken)) {
            joiner.add("session_token=" + sessionToken.toString());
        }
        if (acrValues != null && acrValues.getValues() != null && !acrValues.getValues().isEmpty() ) {
            joiner.add("acr_values=" + acrValues.toString());
        }
        if (data != null && !data.isEmpty()) {
            data.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .map(entry -> joiner.add(entry.getKey() + "=" + entry.getValue().toString()));
        }
        return createRedirectURI(location, joiner.toString());
    }

    private static String createRedirectURI(String url, String parameter) {
        if (parameter == null || parameter.isEmpty()) {
            return url;
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
