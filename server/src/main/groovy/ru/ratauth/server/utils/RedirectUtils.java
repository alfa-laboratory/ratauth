package ru.ratauth.server.utils;

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


    public static String createRedirectURI(String url, String parameter) {
        if (parameter.isEmpty()) {
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
