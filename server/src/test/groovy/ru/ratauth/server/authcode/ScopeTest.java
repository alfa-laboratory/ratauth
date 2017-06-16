package ru.ratauth.server.authcode;

import org.junit.Test;
import ru.ratauth.server.scope.Scope;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ScopeTest {

    @Test
    public void testValueOf() {
        String value = "read:write";

        List<String> scopes = Scope.valueOf(value).getScopes();

        assertEquals("read", scopes.get(0));
        assertEquals("write", scopes.get(1));
    }

    @Test
    public void testToString() throws Exception {

        Scope scope = Scope.builder()
                .scope("mobile.read")
                .scope("mobile.write")
                .build();

        assertEquals("mobile.read:mobile.write", scope.toString());

    }

}
