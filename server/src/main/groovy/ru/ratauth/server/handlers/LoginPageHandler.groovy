package ru.ratauth.server.handlers

import org.springframework.stereotype.Component
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context

import static ratpack.groovy.Groovy.groovyMarkupTemplate

/**
 * @author mgorelikov
 * @since 11/11/16
 */
@Component
class LoginPageHandler implements Action<Chain> {

  @Override
  void execute(Chain chain) throws Exception {
    chain.get('login') { Context ctx ->
      ctx.render groovyMarkupTemplate('login.gtpl',
          title       :'Authorization',
          error       :null,
          method      :'post',
          action      :'/authorize',
          audValue    :ctx.request.queryParams.get('aud'),
          scope       :ctx.request.queryParams.get('scope'),
          clientId    :ctx.request.queryParams.get('client_id'),
          responseType:'code',
          redirectUri :ctx.request.queryParams.get('redirect_uri')
      )
    }

    chain.fileSystem 'public', { f -> f.files() }
  }
}
