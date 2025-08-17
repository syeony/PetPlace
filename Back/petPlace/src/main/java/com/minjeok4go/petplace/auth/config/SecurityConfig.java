package com.minjeok4go.petplace.auth.config;

import com.minjeok4go.petplace.auth.filter.RequestLoggingFilter;
import com.minjeok4go.petplace.auth.jwt.JwtAuthenticationEntryPoint;
import com.minjeok4go.petplace.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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
        System.out.println("🔥 SecurityConfig: 동네 인증 API 허용 설정 중...");

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // 🔽 인증 없이 누구나 접근 가능한 경로들
                        .requestMatchers(
                                // 🔥 동네 인증 관련 API
                                "/api/user/me/dong-authentication",
                                "/api/user/me/dong-authentication/**",
                                "/api/user/test/region-by-coordinates",

                                // Swagger 관련 경로
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**", "/favicon.ico",

                                // 테스트 페이지
                                "/test/**",

                                // 사용자 가입/인증 관련
                                "/api/user/signup", "/api/user/check-username", "/api/user/check-nickname",
                                "/api/user/certifications/prepare", "/api/user/test-portone-token", "/api/user/test-portone-cert/**",

                                // 소셜로그인 및 인증
                                "/api/auth/**",
                                "/api/auth/social/**",

                                // 추천 API
                                "/api/recommend/group", "/api/recommend/batch",

                                // 파일 업로드 및 조회
                                "/api/upload/images",
                                "/images/**",

                                // 웹소켓 연결 경로
                                "/ws/**",

                                // PortOne 결제 웹훅 (PortOne 서버가 직접 호출하므로 인증이 없어야 함)
                                "/api/payments/webhook",
                                "/api/payments/webhook/v1",

                                // 에러 페이지
                                "/error"
                        ).permitAll()

                        // 🔽 명시적으로 GET 메소드만 허용할 경로들
                        .requestMatchers(HttpMethod.GET,
                                "/api/hotels/**" // 호텔 정보 조회는 누구나 가능
                        ).permitAll()

                        // 🔼 위에서 설정한 경로 외의 모든 요청은 인증이 필요함
                        .anyRequest().authenticated()
                )
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, RequestLoggingFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/images/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}