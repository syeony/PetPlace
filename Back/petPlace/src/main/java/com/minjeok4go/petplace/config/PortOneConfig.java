package com.minjeok4go.petplace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PortOneConfig {

    @Value("${portone.base-url}")
    private String portOneBaseUrl;

    @Bean(name = "portOneWebClient")
    public WebClient portOneWebClient() {
        return WebClient.builder()
                .baseUrl(portOneBaseUrl)
                .build();
    }
}
