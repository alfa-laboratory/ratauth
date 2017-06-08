package ru.ratauth.server.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RSA256JWTSigner implements JWTSigner {

    private final JWTProperties jwtProperties;

    @SneakyThrows
    public <T, S extends JWTConverter<T>> String createJWT(T object, S jwtConverter) {

        String privateKeyPem = jwtProperties.getPrivateKey().replaceAll("\\s", "");

        DerInputStream derReader = new DerInputStream(Base64.getDecoder().decode(privateKeyPem));

        DerValue[] seq = derReader.getSequence(0);

        if (seq.length < 9) {
            throw new GeneralSecurityException("Could not parse a PKCS1 private key.");
        }

        BigInteger modulus = seq[1].getBigInteger();
        BigInteger publicExp = seq[2].getBigInteger();
        BigInteger privateExp = seq[3].getBigInteger();
        BigInteger prime1 = seq[4].getBigInteger();
        BigInteger prime2 = seq[5].getBigInteger();
        BigInteger exp1 = seq[6].getBigInteger();
        BigInteger exp2 = seq[7].getBigInteger();
        BigInteger crtCoef = seq[8].getBigInteger();

        RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(modulus, privateExp);
        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, publicExp);

        KeyFactory factory = KeyFactory.getInstance("RSA");

        RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(rsaPublicKeySpec);
        RSAPrivateKey privateKey = (RSAPrivateKey) factory.generatePrivate(rsaPrivateKeySpec);
        Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);

        String issuer = jwtProperties.getIssuer();

        String token = jwtConverter.convert(object)
                .withIssuer(issuer)
                .sign(algorithm);

        JWTVerifier verifier = JWT.require(algorithm).build(); //Reusable verifier instance
        DecodedJWT jwt = verifier.verify(token);

        return token;
    }

}
