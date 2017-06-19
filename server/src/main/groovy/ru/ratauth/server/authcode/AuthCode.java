package ru.ratauth.server.authcode;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Wither;
import ru.ratauth.server.scope.Scope;

import java.time.LocalDateTime;

@Builder
@Wither
@Data
public class AuthCode {

    @NonNull
    private final Scope scope;

    @NonNull
    private final LocalDateTime expiresIn;

}
