package com.minjeok4go.petplace.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 소셜 로그인 설정을 관리하는 Configuration 클래스
 */
@Configuration
@ConfigurationProperties(prefix = "social")
@Getter
@Setter
public class SocialLoginConfig {

    private Kakao kakao = new Kakao();
    private Naver naver = new Naver();
    private Google google = new Google();

    @Getter
    @Setter
    public static class Kakao {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }

    @Getter
    @Setter
    public static class Naver {
        private String clientId;
        private String clientSecret;
    }

    @Getter
    @Setter
    public static class Google {
        private String clientId;
        private String clientSecret;
    }
}