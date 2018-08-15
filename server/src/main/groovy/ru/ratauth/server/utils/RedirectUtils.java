package ru.ratauth.server.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.utils.StringUtils;
import ru.ratauth.utils.URIUtils;

public class RedirectUtils {

    public static String createRedirectURI(RelyingParty relyingParty, String redirectUri) {
        if (StringUtils.isBlank(redirectUri)) {
            return relyingParty.getAuthorizationRedirectURI();
        } else {
            if (!URIUtils.compareHosts(redirectUri, relyingParty.getRedirectURIs()))
                throw new AuthorizationException(AuthorizationException.ID.REDIRECT_NOT_CORRECT);
            else
                return redirectUri;
        }
    }

    public static String createRedirectURIWithPath(RelyingParty relyingParty, String path) {
            return relyingParty.getAuthorizationRedirectURI() + path;
    }

    public static String createRedirectURI(String url, String parameter) {
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

    public static String createRedirectURI(String url, Map<String, String> parameter) {
        if (parameter.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        if (url.contains("?")) {
            if (!url.endsWith("?")) {
                sb.append("&");
            }
        } else {
            sb.append("?");
        }

        return sb.toString() + parameter.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + "=" + silentEncode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    @SneakyThrows(UnsupportedEncodingException.class)
    private static String silentEncode(String value) {
        return URLEncoder.encode(value, "UTF-8");
    }

}
