package com.minjeok4go.petplace.auth.config;

import com.minjeok4go.petplace.auth.filter.RequestLoggingFilter;
import com.minjeok4go.petplace.auth.jwt.JwtAuthenticationEntryPoint;
import com.minjeok4go.petplace.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final RequestLoggingFilter requestLoggingFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // ✅ Swagger 관련 경로 (가장 먼저 배치)
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/favicon.ico",

                                // 테스트 페이지 (개발용)
                                "/test/**",

                                // 사용자 API
                                "/api/user/signup",
                                "/api/user/check-username",
                                "/api/user/check-nickname",
                                "/api/user/certifications/prepare",
                                "/api/user/test-portone-token",
                                "/api/user/test-portone-cert/**",

                                // 소셜로그인
                                "/api/auth/social/login",
                                "/api/auth/social/signup",
                                "/api/auth/social/check-linkable",

                                // 인증 API
                                "/api/auth/login",
                                "/api/auth/refresh",

                                // 추천 API
                                "/api/recommend/group",
                                "/api/recommend/batch",

                                // 기타 공개 API
                                "/api/upload/images",
                                "/images/**",
                                "/error",

                                // 채팅 기능 API
                                "/api/chat/**",
                                "/ws/**",
                                "/ws/chat/**",

                                // 호텔 API
                                "/api/hotels/**",
                                "/api/reservations/**",
                                "/api/payments/**",

                                //호텔 결제
                                "/api/payments/webhook",
                                "/api/auth/**"



                        ).permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                // 필터 순서 조정
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, RequestLoggingFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}