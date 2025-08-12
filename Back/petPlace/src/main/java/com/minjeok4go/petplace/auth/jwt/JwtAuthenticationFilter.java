package com.minjeok4go.petplace.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // ✅ SecurityConfig와 완전 동일한 공개 경로 목록
    private static final String[] PERMIT_ALL_PATTERNS = {
            // Swagger 관련 (가장 먼저 체크)
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/favicon.ico",

            // 사용자 API
            "/api/user/signup",
            "/api/user/check-username",
            "/api/user/check-nickname",
            "/api/user/certifications/prepare",  // ✅ 추가
            "/api/user/test-portone-token",
            "/api/user/test-portone-cert/**",

            // 소셜 로그인
            "/api/auth/social/**",

            // 인증 API
            "/api/auth/login",
            "/api/auth/refresh",

            // 추천 API
//            "/api/recommend/group",
            "/api/recommend/batch",
//            "/api/recommend/**",

            // 기타 공개 API
            "/api/upload/images",
            "/images/**",
            "/error",

            // WebSocket (채팅) 관련
            "/ws/**",
            "/api/chat/**"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        log.debug("=== JWT 필터 진입: {} {} ===", method, path);

        // 공개 경로인 경우, 토큰 검증을 생략하고 바로 다음 필터로 진행
        if (isPublicPath(path)) {
            log.debug("✅ 공개 경로이므로 토큰 검증을 생략합니다: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("🔒 인증이 필요한 엔드포인트입니다: {}", path);
        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                // ✅ 토큰이 유효한 경우 Authentication 생성
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("✅ 인증 성공: 사용자 '{}'의 정보를 Security Context에 저장했습니다.", authentication.getName());
            } catch (Exception e) {
                log.error("❌ Authentication 생성 중 오류 발생: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            // 토큰이 없거나 유효하지 않은 경우
            log.debug("❌ 요청 헤더에 유효한 토큰이 없거나, 토큰이 유효하지 않습니다. URI: {}", path);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    // 공개 경로인지 확인하는 헬퍼 메서드
    private boolean isPublicPath(String path) {
        // AntPathMatcher를 사용하여 와일드카드(**) 패턴을 정확하게 비교
        boolean isPublic = Arrays.stream(PERMIT_ALL_PATTERNS)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (isPublic) {
            log.debug("🔓 공개 경로 매칭: {} -> 패턴 중 하나와 일치", path);
        }

        return isPublic;
    }

    // Request Header 에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}