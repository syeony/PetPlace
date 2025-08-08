package com.minjeok4go.petplace.auth.service;

import com.minjeok4go.petplace.user.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 카카오 API 호출 전용 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoApiService {

    @Qualifier("kakaoWebClient")
    private final WebClient kakaoWebClient;

    /**
     * 액세스 토큰으로 카카오에서 사용자 정보 가져오기
     * @param accessToken 카카오에서 발급받은 액세스 토큰
     * @return 검증된 카카오 사용자 정보
     * @throws RuntimeException 토큰이 유효하지 않거나 API 호출 실패 시
     */
    public KakaoUserInfo getUserInfo(String accessToken) {
        try {
            log.info("카카오 API 사용자 정보 요청 시작");
            
            KakaoUserInfo userInfo = kakaoWebClient.get()
                    .uri("/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("카카오 API 호출 실패 - 상태코드: {}", response.statusCode());
                        return Mono.error(new RuntimeException("카카오 API 호출 실패: " + response.statusCode()));
                    })
                    .bodyToMono(KakaoUserInfo.class)
                    .block();

            if (userInfo == null || userInfo.getId() == null) {
                throw new RuntimeException("카카오 API 응답이 올바르지 않습니다.");
            }

            log.info("카카오 API 사용자 정보 요청 성공 - socialId: {}", userInfo.getSocialId());
            return userInfo;

        } catch (Exception e) {
            log.error("카카오 API 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("카카오 사용자 정보 조회에 실패했습니다. 액세스 토큰을 확인해주세요.", e);
        }
    }

    /**
     * 액세스 토큰 유효성 검증
     * @param accessToken 검증할 액세스 토큰
     * @return 토큰 유효 여부
     */
    public boolean validateAccessToken(String accessToken) {
        try {
            getUserInfo(accessToken);
            return true;
        } catch (Exception e) {
            log.warn("카카오 액세스 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}
