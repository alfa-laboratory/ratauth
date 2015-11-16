package ru.ratauth.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.RestTemplate
import ru.ratauth.server.configuration.ProvidersConfiguration
import ru.ratauth.server.handlers.dto.CheckTokenDTO
import ru.ratauth.server.handlers.dto.TokenDTO
import spock.lang.Specification
import org.springframework.http.HttpEntity

import java.nio.charset.Charset

/**
 * @author mgorelikov
 * @since 03/11/15
 */

@SpringApplicationConfiguration(classes = RatAuthApplication.class)
@IntegrationTest(['server.port=50505', 'management.port=0'])
@TestPropertySource(locations = "classpath:application.yml")
class AuthorizationAPISpec extends Specification {
  @Delegate
  RestTemplate client = new RestTemplate()
  @Value('${server.port}')
  String port
  @Autowired
  ObjectMapper objectMapper

  def 'request authorization code'() {
    given:
    def query = 'response_type=code&client_id=www&scope=read&username=login&password=password&aud=stub'
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<String> requestEntity =
      new HttpEntity<String>(query, requestHeaders);
    when:
    ResponseEntity<String> answer = client.exchange('http://localhost:' + port + '/authorize',
      HttpMethod.POST,
      requestEntity,
      String.class)
    then:
    assert answer.statusCode == HttpStatus.FOUND
    assert answer.getHeaders().get("LOCATION").first().contains("code=")
  }

  def 'request token'() {
    given:
    def query = 'grant_type=authorization_code&response_type=token&code=1234'
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<String> requestEntity =
      new HttpEntity<String>(query, createHeaders('id', 'secret'));
    when:
    ResponseEntity<String> answer = client.exchange('http://localhost:' + port + '/token',
      HttpMethod.POST,
      requestEntity,
      String.class)
    TokenDTO token = objectMapper.readValue(answer.body, TokenDTO.class)
    then:
    assert answer.statusCode == HttpStatus.OK
    assert token.accessToken
  }

  def 'implicit request token'() {
    given:
    def query = 'response_type=token&scope=read&username=login&password=password&aud=stub'
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<String> requestEntity =
        new HttpEntity<String>(query, createHeaders('id', 'secret'));
    when:
    ResponseEntity<String> answer = client.exchange('http://localhost:' + port + '/authorize',
        HttpMethod.POST,
        requestEntity,
        String.class)
    then:
    assert answer.statusCode == HttpStatus.FOUND
    assert answer.getHeaders().get("LOCATION").first().contains("token=")
  }

  def 'refresh token'() {
    given:
    def query = 'grant_type=refresh_token&response_type=token&refresh_token=1234'
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<String> requestEntity =
      new HttpEntity<String>(query, createHeaders('id', 'secret'));
    when:
    ResponseEntity<String> answer = client.exchange('http://localhost:' + port + '/token',
      HttpMethod.POST,
      requestEntity,
      String.class)
    TokenDTO token = objectMapper.readValue(answer.body, TokenDTO.class)
    then:
    assert answer.statusCode == HttpStatus.OK
    assert token.accessToken
  }

  def 'check token'() {
    given:
    def query = 'token=' + ProvidersConfiguration.TOKEN
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<String> requestEntity =
      new HttpEntity<String>(query, createHeaders('id', 'secret'));
    when:
    ResponseEntity<String> answer = client.exchange('http://localhost:' + port + '/check_token',
      HttpMethod.POST,
      requestEntity,
      String.class)
    CheckTokenDTO token = objectMapper.readValue(answer.body, CheckTokenDTO.class)
    then:
    assert answer.statusCode == HttpStatus.OK
    assert token.jti == ProvidersConfiguration.TOKEN_ID
  }

  private HttpHeaders createHeaders(String username, String password) {
    return new HttpHeaders() {
      {
        def auth = username + ":" + password;
        def encodedAuth =
          Base64Coder.encode(auth.getBytes(Charset.forName("UTF-8")))
        def authHeader = "Basic " + new String(encodedAuth)
        set("Authorization", authHeader)
      }
    };
  }
}
