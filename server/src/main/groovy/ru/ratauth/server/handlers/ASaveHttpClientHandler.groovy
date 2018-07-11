package ru.ratauth.server.handlers

import org.springframework.stereotype.Component
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.http.client.HttpClient

@Component
class ASaveHttpClientHandler implements Action<Chain> {

    @Override
    void execute(Chain chain) {
        chain.all { ctx ->
            HttpClientHolder.instance = ctx.get(HttpClient)
            ctx.next()
        }
    }

}
