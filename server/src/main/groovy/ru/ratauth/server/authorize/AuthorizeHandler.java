package ru.ratauth.server.authorize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ratpack.exec.Promise;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.http.Headers;
import ratpack.http.Request;
import ratpack.http.TypedData;

@Component

public class AuthorizeHandler implements Action<Chain> {

    @Override
    public void execute(Chain chain) throws Exception {
        chain.get("authorize", context -> {
            context.getRequest().getQueryParams().get("acr");
            context.redirect("");
            context.render("Hello, world!");
        });
    }

}
