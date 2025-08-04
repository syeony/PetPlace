package com.minjeok4go.petplace.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minjeok4go.petplace.auth.config.PortOneConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortOneApiService {

    private final WebClient portOneWebClient;
    private final PortOneConfig portOneConfig;
    private final ObjectMapper objectMapper;

    /**
     * 포트원 액세스 토큰 발급
     */
    public String getAccessToken() {

        log.debug("포트원에서 로드한 API Key: {}", portOneConfig.getApiKey());
        log.debug("포트원에서 로드한 Secret Key: {}", portOneConfig.getSecretKey());


        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("imp_key", portOneConfig.getApiKey());
        requestBody.put("imp_secret", portOneConfig.getSecretKey());

        try {
            String response = portOneWebClient.post()
                    .uri("/users/getToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("포트원 토큰 응답: {}", response);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.get("code").asInt() == 0) {
                String token = jsonNode.get("response").get("access_token").asText();
                log.info("포트원 액세스 토큰 발급 성공");
                return token;
            } else {
                throw new RuntimeException("토큰 발급 실패: " + jsonNode.get("message").asText());
            }

        } catch (Exception e) {
            log.error("포트원 액세스 토큰 발급 실패", e);
            throw new RuntimeException("토큰 발급 중 오류 발생", e);
        }
    }

    /**
     * 본인인증 결과 조회
     */
    public JsonNode getCertificationInfo(String impUid) {
        String accessToken = getAccessToken();

        try {
            String response = portOneWebClient.get()
                    .uri("/certifications/" + impUid)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("포트원 인증 조회 응답: {}", response);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.get("code").asInt() == 0) {
                log.info("본인인증 정보 조회 성공: impUid={}", impUid);
                return jsonNode.get("response");
            } else {
                throw new RuntimeException("인증 정보 조회 실패: " + jsonNode.get("message").asText());
            }

        } catch (Exception e) {
            log.error("본인인증 정보 조회 실패: impUid={}", impUid, e);
            throw new RuntimeException("인증 정보 조회 중 오류 발생", e);
        }
    }
}