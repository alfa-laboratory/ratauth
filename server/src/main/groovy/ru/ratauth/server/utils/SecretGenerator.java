package ru.ratauth.server.utils;


import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

/**
 * @author mgorelikov
 * @since 09/11/15
 */

public class SecretGenerator {
  public static String createSecret() throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    keyGen.init(256);
    SecretKey secretKey = keyGen.generateKey();
    byte[] encoded = secretKey.getEncoded();
    return Base64Coder.encodeLines(encoded);
  }
}