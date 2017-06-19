package ru.ratauth.server.acr;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ratpack.http.Request;
import ratpack.util.MultiValueMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AcrResolverTest {

    @Mock
    private DefaultAcrMatcher defaultAcrMatcher;

    @Mock
    private MfaTokenAcrMatcher mfaTokenAcrMatcher;

    @Mock
    private Request request;

    @Mock
    private MultiValueMap multiValueMap;

    private AcrResolver acrResolver;

    @Before
    public void setUp() {
        when(request.getQueryParams()).thenReturn(multiValueMap);

        acrResolver = new AcrResolver(defaultAcrMatcher, mfaTokenAcrMatcher);
    }

    @Test
    public void testResolveReturnMfaMatcherIfMfaTokenExist() throws Exception {
        when(multiValueMap.containsKey("mfa_token")).thenReturn(true);

        AcrMatcher acrMatcher = acrResolver.resolve(request);

        assertEquals(mfaTokenAcrMatcher, acrMatcher);
    }

    @Test
    public void testResolveReturnDefaultMatcherIfMfaTokenDoesNotExist() throws Exception {
        when(multiValueMap.containsKey("mfa_token")).thenReturn(false);

        AcrMatcher acrMatcher = acrResolver.resolve(request);

        assertEquals(defaultAcrMatcher, acrMatcher);
    }

}