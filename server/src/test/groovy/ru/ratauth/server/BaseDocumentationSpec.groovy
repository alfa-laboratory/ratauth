package ru.ratauth.server

import com.hazelcast.config.Config
import com.hazelcast.config.GroupConfig
import com.hazelcast.config.NetworkConfig
import com.hazelcast.core.Hazelcast
import com.jayway.restassured.builder.RequestSpecBuilder
import com.jayway.restassured.config.RestAssuredConfig
import com.jayway.restassured.specification.RequestSpecification
import org.junit.Rule
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.JUnitRestDocumentation
import ru.ratauth.server.configuration.TestBaseConfiguration
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration

/**
 * @author mgorelikov
 * @since 26/06/16
 */
@SpringBootTest(
        webEnvironment = NONE,
        classes = [TestBaseConfiguration],
        properties = [
                "ratpack.port=8080"
        ]
)
class BaseDocumentationSpec extends Specification {
    @Rule
    JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation('../server/build/docs/generated-snippets/api')

    protected RequestSpecification documentationSpec

    void setupSpec(){
        createHazelcastServer()
    }
    void setup() {
//        createHazelcastServer()
        this.documentationSpec = new RequestSpecBuilder()
                .addFilter(documentationConfiguration(restDocumentation))
                .setConfig(RestAssuredConfig.config().redirect(RestAssuredConfig.config().getRedirectConfig().followRedirects(false)))
                .build()
    }

    private static void createHazelcastServer() {
        Hazelcast.shutdownAll()
        Config config = new Config().setNetworkConfig(new NetworkConfig().setPort(5701).setPublicAddress("localhost"))
                .setGroupConfig(new GroupConfig().setName("ratauth").setPassword("ratauth"))
                .setInstanceName("dev");
        Hazelcast.getOrCreateHazelcastInstance(config);
    }

}
