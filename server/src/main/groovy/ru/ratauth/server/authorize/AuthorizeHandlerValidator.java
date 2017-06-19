package ru.ratauth.server.authorize;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ratpack.handling.Context;
import ratpack.http.Request;
import ru.ratauth.server.handlers.readers.ReadRequestException;
import ru.ratauth.server.jwt.JWTDecoder;
import ru.ratauth.server.mfatoken.MFATokenJWTConverter;

import static ru.ratauth.server.handlers.readers.ReadRequestException.ID.WRONG_REQUEST;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthorizeHandlerValidator {

    private final JWTDecoder jwtDecoder;

    public boolean validate(Context context) {
        verifyQueryParams(context);
        verifyMfaToken(context);
        return true;
    }


    private void verifyQueryParams(Context context) {

        Request request = context.getRequest();

        if (!isExist(request, "acr")) {
            context.error(new ReadRequestException("acr can not be null"));
        }

        if (!isExist(request, "client_id")) {
            context.error(new ReadRequestException("client_id can not be null"));
        }
    }

    private boolean isExist(Request request, String queryParam) {
        return request.getQueryParams().containsKey(queryParam);
    }

    private void verifyMfaToken(Context context) {
        try {
            String mfaToken = context.getRequest().getQueryParams().get("mfa_token");
            if (mfaToken != null) {
                jwtDecoder.verify(mfaToken, new MFATokenJWTConverter());
            }
        } catch (Exception e) {
            context.error(new ReadRequestException(WRONG_REQUEST, e));
        }
    }

}