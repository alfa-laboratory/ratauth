package ru.ratauth.server.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ratpack.handling.Context;
import ratpack.http.Request;
import ru.ratauth.exception.ReadRequestException;

@Component
public class AuthorizeHandlerValidator {

    public boolean validate(Context context) {
        verifyQueryParams(context);
        return true;
    }

    private void verifyQueryParams(Context context) {

        Request request = context.getRequest();

        if (!isExist(request, "client_id")) {
            context.error(new ReadRequestException("client_id can not be null"));
        }
    }

    private boolean isExist(Request request, String queryParam) {
        String param = request.getQueryParams().get(queryParam);
        return !StringUtils.isBlank(param);
    }

}