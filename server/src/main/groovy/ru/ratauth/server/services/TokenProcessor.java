package ru.ratauth.server.services;

import com.nimbusds.jose.JOSEException;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Token;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author mgorelikov
 * @since 03/11/15
 */
public interface TokenProcessor {
  String createToken(String secret, Set<String> baseAddress,
                     Date created, Long expiresIn, String token,
                     Set<String> resourceServers, Map<String, Object> userInfo);

  Map<String,Object> extractUserInfo(String jwt, String secret);
}
