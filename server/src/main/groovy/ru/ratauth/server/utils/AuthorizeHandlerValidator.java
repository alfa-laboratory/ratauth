package ru.ratauth.server.utils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ratpack.handling.Context;
import ratpack.http.Request;
import ru.ratauth.server.handlers.readers.ReadRequestException;
import ru.ratauth.server.jwt.JWTDecoder;

import static ru.ratauth.server.handlers.readers.ReadRequestException.ID.WRONG_REQUEST;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthorizeHandlerValidator {

    private final JWTDecoder jwtDecoder;

    public boolean validate(Context context) {
        verifyQueryParams(context);
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
        String param = request.getQueryParams().get(queryParam);
        return !StringUtils.isBlank(param);
    }

}