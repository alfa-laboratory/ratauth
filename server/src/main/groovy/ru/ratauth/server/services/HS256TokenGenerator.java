package ru.ratauth.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import org.apache.oltu.jose.jws.JWS;
import org.apache.oltu.jose.jws.signature.SignatureMethod;
import org.apache.oltu.jose.jws.signature.impl.SignatureMethodsHMAC256Impl;
import org.apache.oltu.jose.jws.signature.impl.SymmetricKeyImpl;
import org.apache.oltu.oauth2.jwt.JWT;
import org.apache.oltu.oauth2.jwt.io.JWTWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Token;

import java.util.Arrays;
import java.util.Map;

/**
 * @author mgorelikov
 * @since 03/11/15
 */
@Service
public class HS256TokenGenerator implements TokenGenerator {
  private final String keyString;
  private final SignatureMethod signMethod;
  private final SymmetricKeyImpl key;
  private final ObjectMapper jacksonObjectMapper;

  public HS256TokenGenerator(@Value("${auth.key}") String keyString,
                             ObjectMapper jacksonObjectMapper) {
    this.keyString = keyString;
    this.jacksonObjectMapper = jacksonObjectMapper;
    this.signMethod = new SignatureMethodsHMAC256Impl();
    this.key = new SymmetricKeyImpl(Base64Coder.decodeLines(keyString));
  }


  @Override
  public String createToken(RelyingParty relyingParty, Token token, Map<String,String> userInfo) throws JsonProcessingException {
    String signature = new JWS.Builder()
        .setType("JWT")
        .setAlgorithm(signMethod.getAlgorithm())
        .setPayload(jacksonObjectMapper.writeValueAsString(userInfo))
        .sign(signMethod, key)
        .build().getSignature();

   JWT jwt =  new JWT.Builder()
        .setHeaderAlgorithm("RS256")
        .setClaimsSetAudiences(Arrays.asList(relyingParty.getName()))
        .setClaimsSetIssuer("ratauth.ru")
        .setClaimsSetExpirationTime(token.getCreated().getTime())
        .setClaimsSetIssuedAt(token.expiresIn())
        .setSignature(signature).build();
    return new JWTWriter().write(jwt);
  }
}
