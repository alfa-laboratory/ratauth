package ru.ratauth.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.oltu.jose.jws.JWS;
import org.apache.oltu.oauth2.jwt.JWT;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Token;

import java.util.Map;

/**
 * @author mgorelikov
 * @since 03/11/15
 */
public interface TokenGenerator {
  String createToken(RelyingParty relyingParty, Token token, Map<String,String> userInfo) throws JsonProcessingException;
}
