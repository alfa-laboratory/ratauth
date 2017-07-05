package ru.ratauth.server.acr;

import org.junit.Test;
import ru.ratauth.entities.AcrValues;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class AcrValuesTest {

    @Test
    public void testValueOf() {
        String value = "card:sms";

        List<String> acrValues = AcrValues.valueOf(value).getAcrValues();

        assertEquals("card", acrValues.get(0));
        assertEquals("sms", acrValues.get(1));
    }

    @Test
    public void testToString() throws Exception {

        AcrValues acrValue = AcrValues.builder()
                .acr("card")
                .acr("sms")
                .build();

        assertEquals("card:sms", acrValue.toString());
    }

    @Test
    public void testDifferenceRemoveFirstField() {
        AcrValues requiredAcrValues = AcrValues.valueOf("card:account:sms");
        AcrValues existsAcrValue = AcrValues.valueOf("card");

        AcrValues difference = requiredAcrValues.difference(existsAcrValue);

        assertEquals(AcrValues.valueOf("account:sms"), difference);
    }

    @Test
    public void testDifferenceRemoveSecondField() {
        AcrValues requiredAcrValues = AcrValues.valueOf("card:account:sms");
        AcrValues existsAcrValue = AcrValues.valueOf("account");

        AcrValues difference = requiredAcrValues.difference(existsAcrValue);

        assertEquals(AcrValues.valueOf("card:sms"), difference);
    }

}
