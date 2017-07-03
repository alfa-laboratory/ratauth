package ru.ratauth.server.acr;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class AcrValueTest {

    @Test
    public void testValueOf() {
        String value = "card:sms";

        List<String> acrValues = AcrValue.valueOf(value).getAcrValues();

        assertEquals("card", acrValues.get(0));
        assertEquals("sms", acrValues.get(1));
    }

    @Test
    public void testToString() throws Exception {

        AcrValue acrValue = AcrValue.builder()
                .acr("card")
                .acr("sms")
                .build();

        assertEquals("card:sms", acrValue.toString());
    }

    @Test
    public void testDifferenceRemoveFirstField() {
        AcrValue requiredAcrValues = AcrValue.valueOf("card:account:sms");
        AcrValue existsAcrValue = AcrValue.valueOf("card");

        AcrValue difference = requiredAcrValues.difference(existsAcrValue);

        assertEquals(AcrValue.valueOf("account:sms"), difference);
    }

    @Test
    public void testDifferenceRemoveSecondField() {
        AcrValue requiredAcrValues = AcrValue.valueOf("card:account:sms");
        AcrValue existsAcrValue = AcrValue.valueOf("account");

        AcrValue difference = requiredAcrValues.difference(existsAcrValue);

        assertEquals(AcrValue.valueOf("card:sms"), difference);
    }

}
