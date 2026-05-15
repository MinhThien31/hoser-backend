package com.minhthien.hoser_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${app.public.base-url:/}") String publicBaseUrl) {
        return new OpenAPI()
                .info(new Info()
                        .title("HORSE API")
                        .version("1.0")
                        .description("API documentation for HORSE application"))
                .servers(List.of(new Server().url(publicBaseUrl)));
    }
}
