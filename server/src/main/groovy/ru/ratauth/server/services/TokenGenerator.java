package ru.ratauth.server.services;

import com.nimbusds.jose.JOSEException;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Token;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 03/11/15
 */
public interface TokenGenerator {
  String createToken(RelyingParty relyingParty, Token token, Map<String,String> userInfo) throws JOSEException;
}
