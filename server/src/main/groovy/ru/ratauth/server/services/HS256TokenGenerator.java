package ru.ratauth.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.RelyingParty;
import ru.ratauth.entities.Token;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mgorelikov
 * @since 03/11/15
 */
@Service
public class HS256TokenGenerator implements TokenGenerator {
  private final String keyString;
  private final ObjectMapper jacksonObjectMapper;
  private final JWSSigner signer;

  @Autowired
  public HS256TokenGenerator(@Value("${auth.key}") String keyString,
                             ObjectMapper jacksonObjectMapper) throws KeyLengthException {
    this.keyString = keyString;
    this.jacksonObjectMapper = jacksonObjectMapper;
    // Create HMAC signer
    this.signer= new MACSigner(Base64Coder.decodeLines(keyString));
  }


  @Override
  public String createToken(RelyingParty relyingParty, Token token, Map<String, String> userInfo) throws JOSEException {

// Prepare JWT with claims set
    JWTClaimsSet.Builder jwtBuilder =  new JWTClaimsSet.Builder()
      .issuer("http://ratauth.ru")
      .expirationTime(new Date(token.expiresIn()))
      .audience(relyingParty.getName())
      .jwtID(token.getToken())
      .issueTime(token.getCreated());
    userInfo.forEach((key,value) -> jwtBuilder.claim(key, value));

    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtBuilder.build());

// Apply the HMAC protection
    signedJWT.sign(signer);

// Serialize to compact form, produces something like
// eyJhbGciOiJIUzI1NiJ9.SGVsbG8sIHdvcmxkIQ.onO9Ihudz3WkiauDO2Uhyuz0Y18UASXlSc1eS0NkWyA
    return signedJWT.serialize();
  }

}
