package ru.ratauth.server.authorize;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.ratauth.server.authcode.AuthCodeService;
import ru.ratauth.server.configuration.OpenIdConnectDiscoveryProperties;

import java.net.URL;

import static io.restassured.RestAssured.given;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = NONE,
        classes = AuthorizeHandlerTestConfiguration.class,
        properties = {"ratpack.port=5050"}
)
public class AuthorizeHandlerTest {

    private static final String LOCALHOST = "http://localhost";
    private static final String ACR_CARD_ACCOUNT_SMS = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY3IiOiJjYXJkX2FjY291bnQ6c21zIn0.ew-3YGxETmrdKYhbeXOsgU2ccMfoP_G7hWxXtjbyLwo";

    @Value("${ratpack.port}")
    private int ratpackPort;

    @Autowired
    private OpenIdConnectDiscoveryProperties openIdConnectDiscoveryProperties;

    @Autowired
    private AuthCodeService authCodeService;

    @Before
    public void setup() {
        RestAssured.baseURI = LOCALHOST;
        RestAssured.port = ratpackPort;
    }

    @Test
    public void testRedirectToAuthPageWithAcr() throws Exception {

        String responseLocation = when()
                .queryParam("client_id", "mobile-app")
                .queryParam("scope", "read")
                .queryParam("acr", "card")
                .get("/auth")
                .then()
                .statusCode(302)
                .extract()
                .response()
                .getHeader("Location");

        URL location = new URL(responseLocation);
        URL authorizationEndpoint = new URL(openIdConnectDiscoveryProperties.getAuthorizationEndpoint());

        assertEquals(location.getHost(), authorizationEndpoint.getHost());
        assertEquals(location.getPath(), authorizationEndpoint.getPath() + "/card");
        assertQuery(location);
    }

//    @Test
//    public void testRedirectToAuthPageWithAcrFromAccessToken() throws Exception {
//
//        String responseLocation = when()
//                .queryParam("client_id", "mobile-app")
//                .queryParam("scope", "read")
//                .queryParam("acr", "card_account:sms")
//                .queryParam("access_token", ACR_CARD_ACCOUNT_SMS)
//                .get("/auth")
//                .then()
//                .statusCode(302)
//                .extract()
//                .response()
//                .getHeader("Location");
//
//        URL location = new URL(responseLocation);
//        URL authorizationEndpoint = new URL(openIdConnectDiscoveryProperties.getAuthorizationEndpoint());
//
//        assertEquals(location.getHost(), authorizationEndpoint.getHost());
//        assertEquals(location.getPath(), authorizationEndpoint.getPath() + "/card");
//        assertQuery(location);
//    }

    private void assertQuery(URL location) {
        assertTrue(location.getQuery().contains("client_id=mobile-app"));
        assertTrue(location.getQuery().contains("scope=read"));
        assertTrue(location.getQuery().contains("acr=card"));
    }

    private RequestSpecification when() {
        return given().redirects().follow(false).when();
    }

}
