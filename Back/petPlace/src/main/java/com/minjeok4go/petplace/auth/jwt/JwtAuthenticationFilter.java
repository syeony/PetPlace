package com.minjeok4go.petplace.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // 🔥 SecurityConfig와 동일한 공개 경로 목록 - 더 포괄적으로 설정
    private static final List<String> PUBLIC_URLS = Arrays.asList(
            // 사용자 API
            "/api/user/signup",
            "/api/user/check-username", 
            "/api/user/check-nickname",
            // 인증 API
            "/api/auth/login",
            "/api/auth/refresh",
            // Swagger 관련 - 모든 패턴 포함
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            "/favicon.ico",
            "/error",
            "/api/chat",
            "/ws",
            "/ws/chat"
    );

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.debug("=== JWT 필터 진입: {} {} ===", method, requestURI);

        // 인증이 필요없는 경로는 바로 통과
        if (isPublicEndpoint(requestURI)) {
            log.debug("공개 엔드포인트로 필터 통과: {} {}", method, requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("인증이 필요한 엔드포인트: {} {}", method, requestURI);

        try {
            String token = resolveToken(request);

            if (token == null) {
                log.debug("토큰이 없는 요청: {} {}", method, requestURI);
                sendErrorResponse(response, "토큰이 필요합니다");
                return;
            }

            if (!jwtTokenProvider.validateToken(token)) {
                log.debug("유효하지 않은 토큰: {} {}", method, requestURI);
                sendErrorResponse(response, "유효하지 않은 토큰입니다");
                return;
            }

            // 토큰이 유효한 경우 인증 정보 설정
            String userName = jwtTokenProvider.getUserNameFromToken(token);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userName, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("인증 성공: {}", userName);

        } catch (Exception e) {
            log.error("JWT 필터 에러: ", e);
            sendErrorResponse(response, "토큰 처리 중 오류가 발생했습니다");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 공개 엔드포인트 판별 (SecurityConfig와 동일한 패턴)
     */
    private boolean isPublicEndpoint(String requestURI) {
        // 정확한 매칭
        for (String publicUrl : PUBLIC_URLS) {
            if (requestURI.equals(publicUrl)) {
                log.debug("정확한 매칭: {}", publicUrl);
                return true;
            }
            
            // startsWith 매칭 (swagger, webjars 등)
            if (requestURI.startsWith(publicUrl)) {
                log.debug("startsWith 매칭: {}", publicUrl);
                return true;
            }
        }

        // 🔥 추가: 특정 swagger 파일들 개별 허용
        if (requestURI.contains("swagger") || 
            requestURI.contains("api-docs") || 
            requestURI.contains("webjars")) {
            log.debug("Swagger 관련 경로 허용: {}", requestURI);
            return true;
        }

        return false;
    }

    /**
     * 요청에서 JWT 토큰 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 에러 응답 전송
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        // 기존 ApiResponse 형태로 응답
        String jsonResponse = String.format(
                "{\"success\": false, \"message\": \"%s\", \"status\": 401}",
                message
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
