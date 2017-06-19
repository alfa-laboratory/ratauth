package ru.ratauth.server.acr;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ratpack.http.Request;
import ratpack.util.MultiValueMap;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MfaTokenAcrMatcherTestConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
public class MFATokenAcrMatcherTest {

    private static final String CARD_SMS_ACR = "card:sms";
    private static final String MFA_TOKEN_WITH_CARD = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhY3IiOiJjYXJkIiwic2NvcGUiOiJtb2JpbGUucmVhZCIsImlzcyI6ImFsZmEtYmFuayIsImlkIjoiMSIsImV4cCI6OTQ2Njc0MDAwfQ.VUDADpmnC9NDO6UBUREWjUFe_fisMTWVdfWw4TgvNFg";

    @Mock
    private Request request;

    @Mock
    private MultiValueMap multiValueMap;

    @Autowired
    private AcrMatcher updateTokenAcrMatcher;

    @Before
    public void setUp() {
        when(request.getQueryParams()).thenReturn(multiValueMap);
        when(multiValueMap.get("acr")).thenReturn(CARD_SMS_ACR);
    }

    @Test
    public void testMatchReturnSmsAcrWhenCardAlreadyAuth() throws Exception {
        when(multiValueMap.get("mfa_token")).thenReturn(MFA_TOKEN_WITH_CARD);

        String acr = updateTokenAcrMatcher.match(request);

        assertEquals("sms", acr);
    }

}