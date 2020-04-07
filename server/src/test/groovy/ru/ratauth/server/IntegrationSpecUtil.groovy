package ru.ratauth.server

import io.restassured.http.Header
import org.springframework.http.HttpHeaders

import java.nio.charset.Charset

/**
 * @author djassan
 * @since 11/09/16
 */
class IntegrationSpecUtil {

    static Header createAuthHeaders(String username, String password) {
        def auth = username + ":" + password;
        def encodedAuth =
                Base64.getEncoder().encode(auth.getBytes(Charset.forName("UTF-8")))
        def authHeader = "Basic " + new String(encodedAuth)
        return new Header(HttpHeaders.AUTHORIZATION, authHeader)
    }

}
