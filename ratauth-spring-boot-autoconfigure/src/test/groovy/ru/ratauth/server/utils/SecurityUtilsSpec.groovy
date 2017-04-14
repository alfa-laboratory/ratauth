package ru.ratauth.server.utils

import org.apache.commons.codec.binary.Hex
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * @author tolkv
 * @version 14/03/2017
 */
class SecurityUtilsSpec extends Specification {
  @Unroll
  def 'should generate valid serializable password hash from #passwordRaw and #saltRaw'() {

    given: 'Raw password and Raw salt. Convert to hex and digest representation'
    def hashedPassword = SecurityUtils.hashPassword(passwordRaw, Base64.getEncoder().encodeToString(saltRaw.bytes))
    MessageDigest digest = MessageDigest.getInstance("SHA-256")
    byte[] digestBytes = digest.digest("$passwordRaw$saltRaw".getBytes(StandardCharsets.UTF_8))
    def hexString = Hex.encodeHexString(digestBytes)

    expect: 'serialized password with salt equals to password from shell script'
    println """
    string merge         : ${Base64.getEncoder().encodeToString(digestBytes)}
    utils function       : $hashedPassword
    digest               : ${hexString}
    """.stripIndent().stripMargin()

    hashedPassword == expectedHashBase64

    where:
    passwordRaw                        | saltRaw        | expectedHashBase64 // expectedResult get by command: echo -n $passwordRaw$saltRaw | openssl dgst -binary -sha256 | openssl base64`
    'LGTWg8KbPWBTlFch1Umvwzzju5+V/M7Q' | 'nuA7kFDeXBU=' | 'qCkaHq3avt5bDnCT2z0lAnkt/iLBE8KaXyZmMVwCvEo='
    'fsdfsd'                           | 'itisworse'    | 'qXDXeD7GoT28ofzSRwWi/JjHzCYaRIX8HIWElyNfJoQ='
    'S//uw72kInWcCV7PwH/flGDu/z+ZXJcN' | 'QZNIrBv8RXw=' | 'pY6LK39Qg9tSDYMOcBUud9u+fk0W+epqrdeNg4OlhSs='
  }

}
