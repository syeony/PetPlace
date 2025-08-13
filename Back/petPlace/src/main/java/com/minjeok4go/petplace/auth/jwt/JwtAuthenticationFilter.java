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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Request Header에서 토큰을 추출합니다.
        String token = resolveToken(request);

        // 2. 토큰이 존재하고 유효하다면, Authentication 객체를 생성하여 SecurityContext에 저장합니다.
        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("✅ 인증 성공: 사용자 '{}'의 정보를 Security Context에 저장했습니다.", authentication.getName());
            } catch (Exception e) {
                log.error("❌ 토큰에서 인증 정보를 가져오는 중 오류가 발생했습니다: {}", e.getMessage());
                // 오류 발생 시 SecurityContext를 깨끗하게 비웁니다.
                SecurityContextHolder.clearContext();
            }
        }
        // 토큰이 없거나 유효하지 않은 경우, 아무것도 하지 않고 다음 필터로 넘어갑니다.
        // 이 경우 SecurityContext에는 인증 정보가 없으므로, 뒤따르는 필터(특히 AuthorizationFilter)가
        // SecurityConfig에 정의된 규칙에 따라 접근을 허용하거나 차단할 것입니다.

        // 3. 다음 필터로 요청을 전달합니다.
        filterChain.doFilter(request, response);
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
