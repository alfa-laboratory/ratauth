package ru.ratauth.server.utils;


import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * @author mgorelikov
 * @since 09/11/15
 */

public class SecurityUtils {
    public static String createSecret() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();
        byte[] encoded = secretKey.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }

    public static String createSalt() {
        final Random r = new SecureRandom();
        byte[] salt = new byte[32];
        r.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    @SneakyThrows
    public static String hashPassword(String password, String salt) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        byte[] allBytes = ArrayUtils.addAll(password.getBytes(StandardCharsets.UTF_8), saltBytes);
        byte[] hash = digest.digest(allBytes);
        return Base64.getEncoder().encodeToString(hash);
    }
}