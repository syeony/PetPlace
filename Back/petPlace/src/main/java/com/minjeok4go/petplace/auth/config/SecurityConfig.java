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
// AntPathRequestMatcher는 더 이상 import할 필요가 없습니다.
// import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
                        // 🔥 new AntPathRequestMatcher를 제거하고 문자열만 사용합니다.
                        .requestMatchers(
                                // 사용자 API
                                "/api/user/signup",
                                "/api/user/check-username",
                                "/api/user/check-nickname",
                                "/api/user/certifications/prepare",  // 본인인증 준비 API (DEPRECATED)
                                "/api/user/certifications/otp/request",  // 본인인증 OTP 요청
                                "/api/user/certifications/otp/confirm",  // 본인인증 OTP 확인
                                // 인증 API
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/user/test-portone-token",
                                "/api/user/test-portone-cert/**",

                                // Swagger 관련
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/error",

                                // 채팅 기능 API
                                "/api/chat/**",
                                "/ws/**",      // SockJS endpoint
                                "/ws/chat/**"
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
