package com.minjeok4go.petplace.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
@Slf4j
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);      // 커넥션 타임아웃: 5초
        factory.setReadTimeout(10000);        // 읽기 타임아웃: 10초

        RestTemplate restTemplate = new RestTemplate(factory);

        // 에러 핸들러 설정
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus statusCode = HttpStatus.resolve(response.getStatusCode().value());
                if (statusCode != null) {
                    return statusCode.is4xxClientError() || statusCode.is5xxServerError();
                }
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                log.error("포트원 API 호출 오류 - status: {}", response.getStatusCode());
                throw new RestClientException("포트원 API 호출 실패: " + response.getStatusCode());
            }
        });

        return restTemplate;
    }
}
