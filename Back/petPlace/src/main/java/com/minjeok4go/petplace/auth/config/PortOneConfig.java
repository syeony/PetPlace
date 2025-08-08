package com.minjeok4go.petplace.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 포트원 설정 프로퍼티 (WebClient Bean은 WebClientConfig에서 관리)
 */
@Configuration
@ConfigurationProperties(prefix = "portone")
@Data
public class PortOneConfig {

    private String apiKey;
    private String secretKey;
    private String impCode;      // 포트원 식별코드
    private String storeId;
    private String channelKey;
    private String baseUrl = "https://api.iamport.kr";
    
    // WebClient Bean 정의 제거 (WebClientConfig에서 관리)
}
