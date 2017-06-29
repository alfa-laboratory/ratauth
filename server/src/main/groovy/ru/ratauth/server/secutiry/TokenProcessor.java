package ru.ratauth.server.secutiry;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 03/11/15
 */
public interface TokenProcessor {
    String JWT_SUB = "sub";

    String createToken(String clientId, String secret, String identifier,
                       Date created, Date expiresIn,
                       Set<String> audience, Set<String> scopes, Set<String> authContext,
                       String userId, Map<String, Object> userInfo);

    Map<String, Object> extractInfo(String jwt, String secret);

    Map<String, Object> filterUserInfo(Map<String, Object> info);

    Set<String> extractAuthContext(Map<String, Object> info);

}
