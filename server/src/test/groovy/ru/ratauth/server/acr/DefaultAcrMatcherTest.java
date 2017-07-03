package ru.ratauth.server.acr;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ratpack.http.Request;
import ratpack.util.MultiValueMap;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAcrMatcherTest {

    private static final String CARD_SMS_ACR = "card:sms";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private AcrMatcher defaultAcrMatcher = new DefaultAcrMatcher();

    @Mock
    private Request request;

    @Mock
    private MultiValueMap multiValueMap;

    @Before
    public void setUp() {
        when(request.getQueryParams()).thenReturn(multiValueMap);
    }


    @Test
    public void testMatchSuccess() throws Exception {
        when(multiValueMap.get("acr")).thenReturn(CARD_SMS_ACR);

        String acr = defaultAcrMatcher.match(request);

        assertEquals("card", acr);
    }

    @Test
    public void testMatchShouldThrowNPEWhenAcrIsNull() {
        when(multiValueMap.get("acr")).thenReturn(null);

        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("acr can not be null");

        defaultAcrMatcher.match(request);
    }

}