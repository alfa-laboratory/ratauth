package ru.ratauth.server.configuration.renderer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ratpack.handling.Context;
import ratpack.render.RendererSupport;

/**
 * @author mgorelikov
 * @since 29/10/15
 */
@Configuration
public class RenderedConfiguration {

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Bean
    public RendererSupport<JsonSerializable> jsonRenderer() {
        return new RendererSupport<JsonSerializable>() {
            @Override
            public void render(Context context, JsonSerializable jsonSerializable) throws Exception {
                context.render(jacksonObjectMapper.writeValueAsString(jsonSerializable));
            }
        };
    }
}
