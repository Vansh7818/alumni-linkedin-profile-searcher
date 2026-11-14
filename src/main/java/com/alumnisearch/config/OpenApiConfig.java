package com.alumnisearch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Alumni LinkedIn Profile Searcher API")
                        .version("1.0.0")
                        .description("RESTful microservice developed with Spring Boot to discover, extract, and persist " +
                                "LinkedIn profiles of university alumni using the PhantomBuster API. " +
                                "Supports filtering, deduplication, search analytics, and PostgreSQL database persistence.")
                        .contact(new Contact()
                                .name("Senior Engineering Team")
                                .email("engineering@alumnisearch.com")
                                .url("https://github.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://spring.io/projects/spring-boot")))
                .servers(List.of(
                        new Server().url("/").description("Current Server Context"),
                        new Server().url("http://localhost:" + serverPort).description("Local Development Server")
                ));
    }
}
