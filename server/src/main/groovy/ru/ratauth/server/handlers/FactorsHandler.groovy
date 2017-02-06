package ru.ratauth.server.handlers

import io.netty.handler.codec.http.HttpResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ratpack.error.ServerErrorHandler
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ru.ratauth.interaction.EnrollmentRequest
import ru.ratauth.providers.assurance.dto.AssuranceStatus
import ru.ratauth.server.handlers.dto.EnrollmentDTO
import ru.ratauth.server.handlers.dto.EnrollmentRequestDTO
import ru.ratauth.server.handlers.dto.FactorActivatedDTO
import ru.ratauth.server.handlers.dto.FactorData
import ru.ratauth.server.handlers.dto.FactorListDTO
import ru.ratauth.server.services.AuthAssuranceService

import static ratpack.jackson.Jackson.fromJson
import static ratpack.jackson.Jackson.json
import static ratpack.rx.RxRatpack.observe
import static ru.ratauth.server.handlers.readers.RequestUtil.extractAuth

/**
 * @author mgorelikov
 * @since 01/02/17
 */
@Component
class FactorsHandler implements Action<Chain> {

  @Autowired
  AuthAssuranceService authAssuranceService


  public static final String PROVIDER_NAME = 'providerName'
  public static
  final String ACTIVATION_URL = 'http://ratauth.ru/oidc/factor/16080bf6-dbe4-428e-b648-06739b59e920/activate'

  @Override
  void execute(Chain chain) throws Exception {
    chain.prefix('factor', { factorChain ->
      factorChain.post('enroll', { Context ctx ->
        ctx.byMethod { meth ->
          meth.post {
            observe ctx.parse(fromJson(EnrollmentRequestDTO.class)) flatMap {
              res ->
                def clientCred = extractAuth(ctx.request.getHeaders())
                authAssuranceService.enroll(EnrollmentRequest.builder()
                    .accessToken(res.accessToken)
                    .acrValues(res.acrValues)
                    .clientId(clientCred[0])
                    .clientPassword(clientCred[1])
              )
            } subscribe({
                res -> ctx.render(json(
                    new EnrollmentDTO(
                        enrollmentId:'16080bf6-dbe4-428e-b648-06739b59e920',
                        enrollmentURL:'http://ratauth.ru/oidc/factor/16080bf6-dbe4-428e-b648-06739b59e920',
                        status:AssuranceStatus.ENROLLED
                    )
                  ))
              }, { /*on error*/
                throwable -> ctx.get(ServerErrorHandler).error(ctx, throwable)
              }
            )
          }
        }
      }).post(':enrollId/activate', { Context ctx ->
        ctx.byMethod { meth ->
          meth.post {
            ctx.render(json(
                new FactorActivatedDTO(
                    requiredFields:[
                        [
                            'name':'code',
                            'type':'string',
                            'length':'4'
                        ] as Map
                    ] as List
                )
            ))
          }
        }
      }).post(':enrollId/verify', { Context ctx ->
        ctx.byMethod { meth ->
          meth.post {
            ctx.redirect(HttpResponseStatus.FOUND.code(),
                'http://relyingparty.com/verified?id_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9' +
                    '.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9' +
                    '.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ')
          }
        }
      }).get(':enrollId/list', { Context ctx ->
        ctx.byMethod { meth ->
          meth.get {
            ctx.render(json(
                new FactorListDTO(
                    factors:[
                        new FactorData(
                            factorType:'SMS',
                            provider:PROVIDER_NAME,
                            activationURL:ACTIVATION_URL
                        ),
                        new FactorData(
                            factorType:'PUSH',
                            provider:PROVIDER_NAME,
                            activationURL:ACTIVATION_URL
                        )
                    ] as List
                )
            ))
          }
        }
      })
    })
  }
}
