package ru.ratauth.server.secutiry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author mgorelikov
 * @since 03/11/15
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HS256TokenProcessor implements TokenProcessor {
  private final ObjectMapper jacksonObjectMapper;
  private static final String SCOPE = "scope";

  @Value("${auth.token.issuer}")
  private String issuer;//final

  @Override
  @SneakyThrows
  public String createToken(String secret, String identifier,
                            Date created, Date expiresIn,
                            Set<String> audience, Set<String> scopes,
                            String userId, Map<String, Object> userInfo) {
    final JWSSigner signer = new MACSigner(Base64Coder.decodeLines(secret));
// Prepare JWT with claims set
    JWTClaimsSet.Builder jwtBuilder = new JWTClaimsSet.Builder()
        .issuer(issuer)
        .subject(userId)
        .expirationTime(expiresIn)
        .audience(new ArrayList<>(audience))
        .claim(SCOPE, scopes)
        .jwtID(identifier)
        .issueTime(created);
    userInfo.forEach((key, value) -> jwtBuilder.claim(key, value));

    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtBuilder.build());

// Apply the HMAC protection
    signedJWT.sign(signer);

// Serialize to compact form, produces something like
// eyJhbGciOiJIUzI1NiJ9.SGVsbG8sIHdvcmxkIQ.onO9Ihudz3WkiauDO2Uhyuz0Y18UASXlSc1eS0NkWyA
    return signedJWT.serialize();
  }

  @Override
  @SneakyThrows
  public Map<String, Object> extractInfo(String jwt, String secret) {
    SignedJWT signedJWT = SignedJWT.parse(jwt);
    final JWSVerifier verifier = new MACVerifier(Base64Coder.decodeLines(secret));
    if(!signedJWT.verify(verifier))
      throw new JWTVerificationException("User info extraction error");
    return signedJWT.getJWTClaimsSet().getClaims();
  }

  @Override
  public Map<String, Object> filterUserInfo(Map<String, Object> info) {
    Map<String,Object> result = new HashMap<>();
    info.forEach((key,value) -> {
      if(!JWTClaimsSet.getRegisteredNames().contains(key))
        result.put(key, value);
    });
    return result;
  }
}
