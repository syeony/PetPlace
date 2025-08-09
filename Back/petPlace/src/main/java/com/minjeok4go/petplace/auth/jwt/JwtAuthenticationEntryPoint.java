package com.minjeok4go.petplace.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // [개선] 서버 로그에 실제 예외 정보 기록
        log.warn("Unauthorized error: {}", authException.getMessage());
        log.debug("Exception details:", authException);

        // [개선] JwtAuthenticationFilter에서 넘겨준 예외 속성을 확인하여 더 구체적인 메시지 생성
        String errorMessage = (String) request.getAttribute("exception");
        if (errorMessage == null) {
            errorMessage = "인증되지 않은 사용자입니다. 로그인이 필요합니다.";
        }

        sendErrorResponse(response, errorMessage);
    }

    /**
     * 401 Unauthorized 에러 응답을 전송하는 헬퍼 메서드
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태 코드

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
