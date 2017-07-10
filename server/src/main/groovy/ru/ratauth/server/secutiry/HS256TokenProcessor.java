package ru.ratauth.server.secutiry;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.SneakyThrows;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

/**
 * @author mgorelikov
 * @since 03/11/15
 */
@Service
public class HS256TokenProcessor implements TokenProcessor {
    private static final String SCOPE = "scope";
    private static final String CLIENT_ID = "client_id";
    private static final String ACR = "acr";

    private static final List<String> LOCAL_REGISTERED_CLAIMS = asList(SCOPE, CLIENT_ID);

    @Value("${auth.token.issuer}")
    private String issuer;//final

    @Override
    @SneakyThrows
    public String createToken(String clientId, String secret, String identifier,
                              Date created, Date expiresIn,
                              Set<String> audience, Set<String> scopes, Collection<String> authContext,
                              String userId, Map<String, Object> userInfo) {
        final JWSSigner signer = new MACSigner(Base64.getDecoder().decode(secret));
        final List<String> aud = new ArrayList<>(audience);
        aud.add(clientId);
// Prepare JWT with claims set
        JWTClaimsSet.Builder jwtBuilder = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(userId)
                .expirationTime(expiresIn)
                .audience(aud)
                .claim(SCOPE, scopes)
                .claim(CLIENT_ID, clientId)
                .claim(ACR, authContext)
                .jwtID(identifier)
                .issueTime(created);
        userInfo.forEach(jwtBuilder::claim);

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
        final JWSVerifier verifier = new MACVerifier(Base64.getDecoder().decode(secret));
        if (!signedJWT.verify(verifier))
            throw new JWTVerificationException("User info extraction error");
        return signedJWT.getJWTClaimsSet().getClaims();
    }

    @Override
    public Map<String, Object> filterUserInfo(Map<String, Object> info) {
        Map<String, Object> result = new HashMap<>();
        info.forEach((key, value) -> {
            if (!JWTClaimsSet.getRegisteredNames().contains(key) && !LOCAL_REGISTERED_CLAIMS.contains(key))
                result.put(key, value);
        });
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> extractAuthContext(Map<String, Object> info) {
        return info.get(ACR) != null ? ((JSONArray) info.get(ACR)).stream().map(Object::toString).collect(toSet()) : new HashSet<>(asList("account", "sms"));//    @TODO default acr as config
    }
}
