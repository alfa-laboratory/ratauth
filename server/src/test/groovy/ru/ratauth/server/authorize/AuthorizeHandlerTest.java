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
import ru.ratauth.server.configuration.TestBaseConfiguration;
import ru.ratauth.services.ClientService;

import java.net.URL;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = NONE,
        classes = TestBaseConfiguration.class,
        properties = {"ratpack.port=8080"})
public class AuthorizeHandlerTest {

    private static final String CLIENT_ID = "mine";
    private static final String LOCALHOST = "http://localhost";
    private static final String MFA_TOKEN_ACR_CARD = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhY3IiOiJjYXJkIiwic2NvcGUiOiJtb2JpbGUucmVhZCIsImlzcyI6ImFsZmEtYmFuayIsImlkIjoiMSIsImV4cCI6OTQ2Njc0MDAwMH0.vxValRJ4bN0c3KftC0mMx0Fajm4Ub9Bhb-fiuuJSz4g";

    @Value("${ratpack.port}")
    private int ratpackPort;

    @Autowired
    private ClientService clientService;
    private URL authorizationEndpoint;

    @Before
    public void setup() throws Exception {
        RestAssured.baseURI = LOCALHOST;
        RestAssured.port = ratpackPort;
        authorizationEndpoint = new URL(clientService.getRelyingParty(CLIENT_ID)
                .toBlocking()
                .single()
                .getAuthorizationPageURI());
    }

    @Test
    public void testRedirectToAuthPageWithAcr() throws Exception {

        String responseLocation = when()
                .queryParam("client_id", CLIENT_ID)
                .queryParam("scope", "read")
                .queryParam("acr_values", "card:sms")
                .header("Content-Type", "text/html")
                .get("/authorize")
                .then()
                .statusCode(302)
                .extract()
                .response()
                .getHeader("Location");

        URL location = new URL(responseLocation);

        assertEquals(authorizationEndpoint.getHost(), location.getHost());
        assertEquals(authorizationEndpoint.getPath() + "/card", location.getPath());
        assertQuery(location);
    }

    private void assertQuery(URL location) {
        assertTrue(location.getQuery().contains(format("client_id=%s", CLIENT_ID)));
        assertTrue(location.getQuery().contains("scope=read"));
        assertTrue(location.getQuery().contains("acr_values=card"));
    }

    private RequestSpecification when() {
        return given().redirects().follow(false).when();
    }

}
