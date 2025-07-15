package com.ssafy.api.config.security;
import org.springframework.beans.factory.annotation.Value;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

//@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long validityInMilliseconds;

    // ① JwtTokenProvider 를 Bean 으로 등록
    @Bean
    public JwtTokenProvider jwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long validityInMilliseconds,
            UserDetailsService userDetailsService  // 만약 사용자 정보 조회가 필요하면 주입
    ) {
        return new JwtTokenProvider(secretKey, validityInMilliseconds, userDetailsService);

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/css/**", "/js/**", "/img/**", "/lib/**")
                .requestMatchers("/v2/api-docs", "/swagger-resources/**",
                        "/swagger-ui.html", "/webjars/**", "/swagger/**",
                        "/v3/api-docs/**", "/swagger-ui/**");
    }

    @Bean
     public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider) throws Exception {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);

        http
                .httpBasic(httpBasic -> httpBasic.disable()) // rest api 이므로 기본설정 사용안함
                .csrf(csrf -> csrf.disable())
                .headers(headers ->
                        headers.frameOptions(frame ->
                                frame.disable()
                        )
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // permitAll() 처리한 경로의 API는 JWT 값이 없어도 실행 가능
                        .requestMatchers("/api/sign/**", "/api/auth/**").permitAll()
                        .requestMatchers("/docs/**").permitAll()
                        .anyRequest().hasRole("USER")
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/user/denied")
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("1234"))
                .roles("ADMIN")
                .build();

        UserDetails guest = User.builder()
                .username("guest")
                .password(passwordEncoder().encode("guest"))
                .roles("GUEST")
                .build();

        return new InMemoryUserDetailsManager(admin, guest);
    }
}