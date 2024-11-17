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

    @Value("${app.openapi.name}")
    private String applicationName;

    @Value("${app.openapi.description}")
    private String description;

    @Value("${app.openapi.version}")
    private String applicationVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        // List<Server> servers = new ArrayList<>();
        // servers.add(new Server().url(serverUrl).description("Primary Server"));

        // 如果有額外的 URL，可以在這裡加入，例如其他環境
        // servers.add(new Server().url("http://api.example.com").description("Production Server"));

        return new OpenAPI()
                .info(new Info()
                        .title(applicationName)
                        .description(description)
                        .version(applicationVersion))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("http://api.example.com").description("Production Server")));

    }

}
