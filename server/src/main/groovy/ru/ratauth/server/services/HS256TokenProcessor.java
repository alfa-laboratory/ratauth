package ru.ratauth.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import com.nimbusds.jose.*;
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
  private static final String RP_BASE_ADDRESS = "rp_base_address";
  public static final String INT_PREFIX="i_";

  @Value("${auth.token.issuer}")
  private String issuer;//final

  @Override
  @SneakyThrows
  public String createToken(String secret, Set<String> baseAddress,
                            Date created, Long expiresIn, String token,
                            Set<String> resourceServers, Map<String, Object> userInfo) {
    final JWSSigner signer = new MACSigner(Base64Coder.decodeLines(secret));
// Prepare JWT with claims set
    JWTClaimsSet.Builder jwtBuilder = new JWTClaimsSet.Builder()
        .issuer(issuer)
        .expirationTime(new Date(expiresIn))
        .audience(new ArrayList<>(resourceServers))
        .jwtID(token)
        .issueTime(created);
    jwtBuilder.claim(RP_BASE_ADDRESS, baseAddress);
    userInfo.forEach((key, value) -> jwtBuilder.claim(INT_PREFIX + key, value));

    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtBuilder.build());

// Apply the HMAC protection
    signedJWT.sign(signer);

// Serialize to compact form, produces something like
// eyJhbGciOiJIUzI1NiJ9.SGVsbG8sIHdvcmxkIQ.onO9Ihudz3WkiauDO2Uhyuz0Y18UASXlSc1eS0NkWyA
    return signedJWT.serialize();
  }

  @Override
  @SneakyThrows
  public Map<String, Object> extractUserInfo(String jwt, String secret) {
    SignedJWT signedJWT = SignedJWT.parse(jwt);
    final JWSVerifier verifier = new MACVerifier(Base64Coder.decodeLines(secret));
    if(!signedJWT.verify(verifier))
      throw new JWTVerificationException("User info extraction error");
    Map<String,Object> result = new HashMap<>();
    signedJWT.getJWTClaimsSet().getClaims().forEach((key,value) -> {
      if(key.startsWith(INT_PREFIX))
        result.put(key.substring(INT_PREFIX.length(), key.length()),value);
    });
    return result;
  }
}
