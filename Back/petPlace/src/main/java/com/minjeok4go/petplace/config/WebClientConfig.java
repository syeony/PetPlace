package com.minjeok4go.petplace.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@RequiredArgsConstructor
@EnableConfigurationProperties(AiProps.class) // ← AiProps 활성화
@Configuration
public class WebClientConfig {

    @Value("${portone.base-url}")
    private String portOneBaseUrl;

    /**
     * 기본 WebClient (범용)
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
    
    /**
     * 카카오 API 호출용 WebClient
     */
    @Bean("kakaoWebClient")
    public WebClient kakaoWebClient() {
        return WebClient.builder()
                .baseUrl("https://kapi.kakao.com")
                .build();
    }
    
    /**
     * 포트원 API 호출용 WebClient
     */
    @Bean("portOneWebClient")
    public WebClient portOneWebClient() {
        return WebClient.builder()
                .baseUrl(portOneBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    @Qualifier("aiWebClient")
    WebClient aiWebClient(@Value("${ai.similarity.base-url}") String baseUrl,
                          @Value("${service.internal-token}") String token) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Service-Token", token)
                .build();
    }
}
