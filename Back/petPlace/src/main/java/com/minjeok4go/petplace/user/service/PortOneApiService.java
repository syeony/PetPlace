package com.minjeok4go.petplace.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minjeok4go.petplace.auth.config.PortOneConfig;
import com.minjeok4go.petplace.user.dto.VerificationData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
     * 본인인증 URL 생성 (간단한 방식)
     */
    public Map<String, String> prepareCertification() {
        log.info("포트원 본인인증 URL 생성 시작");

        try {
            // 고유한 merchant_uid 생성
            String merchantUid = "cert_" + System.currentTimeMillis();
            log.info("생성된 merchant_uid: {}", merchantUid);

            // 포트원 식별코드 (환경변수나 설정에서 가져와야 함)
            String impCode = portOneConfig.getImpCode(); // 포트원 식별코드
            
            // 본인인증 URL 직접 구성
            String certificationUrl = String.format(
                "https://cert.iamport.kr/?IMP=%s&merchant_uid=%s&m_redirect_url=%s",
                impCode,
                merchantUid,
                "petplace://certification" // 모바일 앱 딥링크로 변경
            );
            
            log.info("본인인증 URL 생성 성공: {}", certificationUrl);
            
            Map<String, String> result = new HashMap<>();
            result.put("certification_url", certificationUrl);
            result.put("merchant_uid", merchantUid);
            return result;

        } catch (Exception e) {
            log.error("포트원 본인인증 URL 생성 중 오류 발생", e);
            throw new RuntimeException("본인인증 URL 생성 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 본인인증 결과 조회 (JsonNode 반환)
     */
    public JsonNode getCertificationInfo(String impUid) {
        log.info("포트원 인증 정보 조회 시작: impUid={}", impUid);

        try {
            // 1. 액세스 토큰 발급
            log.info("포트원 액세스 토큰 발급 시작");
            String accessToken = getAccessToken();
            log.info("포트원 액세스 토큰 발급 성공: {}...", accessToken.substring(0, Math.min(20, accessToken.length())));

            // 2. API 호출
            String url = "/certifications/" + impUid;
            log.info("포트원 API 호출 URL: {}", url);

            String response = portOneWebClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("포트원 API 원본 응답 길이: {}", response != null ? response.length() : "NULL");
            log.info("포트원 API 원본 응답 내용: {}", response);

            // 3. 응답 검증
            if (response == null || response.trim().isEmpty()) {
                log.error("포트원 API 응답이 비어있습니다");
                return null;
            }

            // 4. JSON 파싱
            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(response);
            } catch (Exception parseException) {
                log.error("JSON 파싱 실패: {}", parseException.getMessage());
                throw new RuntimeException("포트원 API 응답 파싱 실패: " + parseException.getMessage());
            }

            // 5. 응답 코드 확인
            JsonNode codeNode = jsonNode.get("code");
            if (codeNode == null) {
                log.error("응답에 code 필드가 없습니다: {}", response);
                throw new RuntimeException("잘못된 포트원 API 응답 형식");
            }

            int code = codeNode.asInt();
            log.info("포트원 API 응답 코드: {}", code);

            if (code == 0) {
                JsonNode responseData = jsonNode.get("response");
                log.info("본인인증 정보 조회 성공: impUid={}", impUid);
                return responseData;
            } else {
                JsonNode messageNode = jsonNode.get("message");
                String message = messageNode != null ? messageNode.asText() : "Unknown error";
                log.error("포트원 API 오류: code={}, message={}", code, message);
                throw new RuntimeException("포트원 API 오류 - 코드: " + code + ", 메시지: " + message);
            }

        } catch (Exception e) {
            log.error("포트원 인증 정보 조회 실패: impUid={}, 오류: {}", impUid, e.getMessage(), e);

            // 예외를 다시 던지되, null을 반환하지 않도록 함
            if (e instanceof RuntimeException) {
                throw e;
            } else {
                throw new RuntimeException("포트원 API 호출 중 오류 발생: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 본인인증 결과를 VerificationData로 변환하여 반환 (소셜 로그인용)
     */
    public VerificationData getVerificationData(String impUid) {
        log.info("본인인증 데이터 변환 시작: impUid={}", impUid);
        
        try {
            JsonNode responseData = getCertificationInfo(impUid);
            
            if (responseData == null || responseData.isMissingNode()) {
                throw new IllegalArgumentException("포트원으로부터 유효한 응답 데이터를 받지 못했습니다.");
            }

            // 본인인증 성공 여부 확인 (certified 필드)
            boolean isCertified = responseData.path("certified").asBoolean(false);
            if (!isCertified) {
                throw new IllegalArgumentException("본인인증이 완료되지 않았습니다.");
            }

            // CI(unique_key) 추출 및 검증
            String ci = responseData.path("unique_key").asText(null);
            if (ci == null || ci.trim().isEmpty()) {
                log.warn("CI(unique_key)가 비어있습니다. 전화번호로 임시 CI 생성");
                String phone = responseData.path("phone").asText();
                ci = "TEMP_CI_" + phone + "_" + System.currentTimeMillis();
            }

            // 인증 시간 검증 (예: 30분 이내)
            long certifiedAt = responseData.path("certified_at").asLong();
            long currentTime = System.currentTimeMillis() / 1000;
            if (currentTime - certifiedAt > 1800) { // 30분 = 1800초
                throw new IllegalArgumentException("본인인증 유효 시간이 초과되었습니다. 다시 시도해주세요.");
            }

            // 생년월일 파싱
            String birthdayString = responseData.path("birthday").asText(null);
            if (birthdayString == null) {
                throw new IllegalArgumentException("생년월일 정보가 없습니다.");
            }
            LocalDate birthDate = LocalDate.parse(birthdayString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            VerificationData result = VerificationData.builder()
                    .ci(ci)
                    .name(responseData.path("name").asText())
                    .phone(responseData.path("phone").asText())
                    .birthDate(birthDate)
                    .gender(responseData.path("gender").asText())
                    .isForeigner(responseData.path("foreigner").asBoolean(false))
                    .build();

            log.info("본인인증 데이터 변환 완료: name={}, phone={}", result.getName(), result.getPhone());
            return result;
            
        } catch (Exception e) {
            log.error("본인인증 데이터 변환 실패: impUid={}, 오류타입: {}, 메시지: {}", impUid, e.getClass().getSimpleName(), e.getMessage(), e);
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("본인인증 검증 중 시스템 오류가 발생했습니다.", e);
        }
    }
}
