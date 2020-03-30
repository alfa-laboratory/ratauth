/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ratauth.server.autoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ratpack.func.Action;
import ratpack.handling.Chain;

/**
 * @author Dave Syer
 * thanks Dave
 */
@Configuration
@ConditionalOnClass({EndpointAutoConfiguration.class})
@AutoConfigureAfter({EndpointAutoConfiguration.class})
@EnableConfigurationProperties
public class RatpackSpringEndpointsAutoConfiguration {

//    @Bean
//    @ConditionalOnMissingBean(ManagementServerProperties.class)
//    public ManagementServerProperties managementServerProperties(ManagementServerProperties xxx) {
//        return new ManagementServerProperties();
//    }

    @Bean
    protected EndpointInitializer ratpackEndpointInitializer() {
        return new EndpointInitializer();
    }

    private static class EndpointInitializer implements Action<Chain> {

        @Autowired(required = false)
        private PrometheusScrapeEndpoint prometheusScrapeEndpoint;

        @Autowired
        private ObjectMapper jacksonObjectMapper;

//        @Autowired
//        private List<Endpoint<?>> endpoints = Collections.emptyList();

        @Autowired
        private ManagementServerProperties management;

        @Override
        public void execute(Chain chain) throws Exception {

            if (prometheusScrapeEndpoint != null) {
                chain.get("actuator/prometheus", context -> context.render(prometheusScrapeEndpoint.scrape()));
            }
            chain.get("actuator/health", context -> context.render("OK"));
            chain.get("health", context -> context.render("OK"));
//            String prefix = management.getContextPath();
//            if (StringUtils.hasText(prefix)) {
//                prefix = prefix.endsWith("/") ? prefix : prefix + "/";
//            } else {
//                prefix = "";
//            }
//            for (Endpoint<?> endpoint : endpoints) {
//                if (endpoint.isEnabled()) {
//                    chain.get(prefix + endpoint.getId(), context ->
//                            {
//                                Object endpointResult = endpoint.invoke();
//                                if (endpointResult.getClass().isAssignableFrom(ResponseEntity.class)) {
//                                    ResponseEntity<?> entity = (ResponseEntity<?>) endpointResult;
//                                    entity.getHeaders().forEach(context::header);
//                                    context.render(entity.getBody());
//                                } else {
//                                    context.render(jacksonObjectMapper.writeValueAsString(endpoint.invoke()));
//                                }
//                            }
//                    );
//                }
//            }
        }

    }

}
