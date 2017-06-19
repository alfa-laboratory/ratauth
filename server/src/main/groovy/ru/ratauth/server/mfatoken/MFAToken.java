package ru.ratauth.server.mfatoken;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import ru.ratauth.server.acr.AcrValue;
import ru.ratauth.server.scope.Scope;

import java.time.LocalDateTime;

@Builder
@Getter
@EqualsAndHashCode(exclude = "scope")
public class MFAToken {

    @NonNull
    private final String id;

    @NonNull
    private final LocalDateTime expiredAt;

    @NonNull
    private final AcrValue acrValue;

    private final Scope scope;

}
