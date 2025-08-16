package com.minjeok4go.petplace.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

// application.yml 값들을 타입이 있는 자바객체로 바인딩 해주는 설정 클래스
@ConfigurationProperties(prefix = "ai.similarity")
public class AiProps {
    private String baseUrl;
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 15000;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
}