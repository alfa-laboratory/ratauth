package ru.ratauth.server.authcode;

import org.junit.Test;
import ru.ratauth.server.scope.Scope;

import static org.junit.Assert.*;

public class ScopeTest {
    @Test
    public void testToString() throws Exception {

        Scope scope = Scope.builder()
                .scope("mobile.read")
                .scope("mobile.write")
                .build();

        assertEquals("mobile.read:mobile.write", scope.toString());

    }

}