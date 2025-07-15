package com.ssafy.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.host:localhost:8080}")
    private String host;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("http://" + host))
                .info(new Info()
                        .title("SSAFY API V1")
                        .version("v1")
                        .description("SSAFY API 문서")
                        .contact(new Contact()
                                .name("Contact Me")
                                .url("https://www.example.com")
                                .email("test@example.com")
                        )
                );
    }
}
