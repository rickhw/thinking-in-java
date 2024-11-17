package com.gtcafe.asimov.platform;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Value("${app.openapi.title}")
    private String openapiTitle;

    @Value("${app.openapi.description}")
    private String description;

    @Value("${app.openapi.version}")
    private String openapiVersion;

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title(openapiTitle)
                        .description(description)
                        .version(openapiVersion))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("http://api.example.com").description("Production Server")));

    }

}
