package ru.ratauth.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.http.*
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import ru.ratauth.server.configuration.PersistenceServiceStubConfiguration
import ru.ratauth.server.handlers.dto.CheckTokenDTO
import ru.ratauth.server.handlers.dto.TokenDTO
import spock.lang.Ignore
import spock.lang.Specification

import java.nio.charset.Charset

/**
 * @author mgorelikov
 * @since 03/11/15
 */

@Ignore
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
    def query = 'response_type=code&client_id='+PersistenceServiceStubConfiguration.CLIENT_NAME+'&scope=rs.read&username=login&password=password'
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

  def 'bad request authorization code'() {
    given:
    def query = 'login=username&password=password'
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
    def e = thrown(HttpClientErrorException)
    e.getMessage().contains('400')
    e.getResponseBodyAsString().contains('response_type')
  }

  def 'bad requisites for authorization code'() {
    given:
    def query = 'response_type=code&client_id='+PersistenceServiceStubConfiguration.CLIENT_NAME+'&scope=rs.read&username=login&password=bad'
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
    def e = thrown(HttpClientErrorException)
    e.getMessage().contains('403')
    e.getResponseBodyAsString().contains('Authorization failed')
  }

  def 'request token'() {
    given:
    def query = 'grant_type=authorization_code&response_type=token&code='+ PersistenceServiceStubConfiguration.CODE
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<String> requestEntity =
      new HttpEntity<String>(query, createHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD));
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

  def 'request token by expired code'() {
    given:
    def query = 'grant_type=authorization_code&response_type=token&code=1111'
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<String> requestEntity =
        new HttpEntity<String>(query, createHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD));
    when:
    client.exchange('http://localhost:' + port + '/token',
        HttpMethod.POST,
        requestEntity,
        String.class)
    then:
    def e = thrown(HttpClientErrorException)
    e.getMessage().contains('419')
    e.getResponseBodyAsString().contains('expired')
  }

  def 'implicit request token'() {
    given:
    def query = 'response_type=token&scope=read&username=login&password=password'
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<String> requestEntity =
        new HttpEntity<String>(query, createHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD));
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
    def query = 'grant_type=refresh_token&response_type=token&refresh_token='+PersistenceServiceStubConfiguration.REFRESH_TOKEN
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<String> requestEntity =
      new HttpEntity<String>(query, createHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD));
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

  def 'request cross-authorization code'() {
    given:
    def query = 'grant_type=authentication_token&response_type=code&client_id='+PersistenceServiceStubConfiguration.CLIENT_NAME+ '2' +
        '&scope=rs.read&refresh_token='+PersistenceServiceStubConfiguration.REFRESH_TOKEN
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<String> requestEntity =
        new HttpEntity<String>(query, createHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD));
    when:
    ResponseEntity<String> answer = client.exchange('http://localhost:' + port + '/authorize',
        HttpMethod.POST,
        requestEntity,
        String.class)
    then:
    assert answer.statusCode == HttpStatus.FOUND
    assert answer.getHeaders().get("LOCATION").first().contains("code=")
  }

  def 'check token'() {
    given:
    def query = 'token=' + PersistenceServiceStubConfiguration.TOKEN
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<String> requestEntity =
      new HttpEntity<String>(query, createHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD));
    when:
    ResponseEntity<String> answer = client.exchange('http://localhost:' + port + '/check_token',
      HttpMethod.POST,
      requestEntity,
      String.class)
    CheckTokenDTO token = objectMapper.readValue(answer.body, CheckTokenDTO.class)
    then:
    assert answer.statusCode == HttpStatus.OK
    assert token.jti.split('\\.').length == 3
    assert token.clientId == PersistenceServiceStubConfiguration.CLIENT_NAME
  }

  def 'get jwt token for external resource server'() {
    given:
    def query = 'token=' + PersistenceServiceStubConfiguration.TOKEN + '&client_id='+PersistenceServiceStubConfiguration.CLIENT_NAME+'3'
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<String> requestEntity =
        new HttpEntity<String>(query, createHeaders(PersistenceServiceStubConfiguration.CLIENT_NAME, PersistenceServiceStubConfiguration.PASSWORD));
    when:
    ResponseEntity<String> answer = client.exchange('http://localhost:' + port + '/check_token',
        HttpMethod.POST,
        requestEntity,
        String.class)
    CheckTokenDTO token = objectMapper.readValue(answer.body, CheckTokenDTO.class)
    then:
    assert answer.statusCode == HttpStatus.OK
    assert token.jti.split('\\.').length == 3
    assert token.clientId == PersistenceServiceStubConfiguration.CLIENT_NAME+'3'
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
