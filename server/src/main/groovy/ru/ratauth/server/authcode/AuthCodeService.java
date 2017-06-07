package ru.ratauth.server.authcode;

import ru.ratauth.server.scope.Scope;

public interface AuthCodeService {

    AuthCode createAuthCode(Scope scope);

}
