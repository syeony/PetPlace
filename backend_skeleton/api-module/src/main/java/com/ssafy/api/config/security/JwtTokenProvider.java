//package com.ssafy.api.config.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jws;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.stereotype.Component;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.servlet.http.HttpServletRequest;
//import java.util.Base64;
//import java.util.Date;
//import java.util.List;
//
////@Component
////@RequiredArgsConstructor
//public class JwtTokenProvider { // JWT 토큰을 생성 및 검증 모듈
//
//    // application yml 설정파일에 설정한 값 사용
//    @Value("${spring.jwt.secret}")
//    private String secretKey;
//
//    private long validityInMilliseconds = 1000L * 60 * 60 * 24 * 30; // 30일만 토큰 유효
//
//    private final UserDetailsService userDetailsService;
//
//    // ① 새로 추가하는 생성자
//    public JwtTokenProvider(
//            @Value("${jwt.secret}") String secretKey,
//            @Value("${jwt.expiration}") long validityInMilliseconds,
//            UserDetailsService userDetailsService  // 만약 사용자 정보 조회가 필요하면 주입
//    ) {
//        this.secretKey = secretKey;
//        this.validityInMilliseconds = validityInMilliseconds;
//        this.userDetailsService = userDetailsService;
//    }
//
//    @PostConstruct
//    protected void init() {
//        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
//    }
//
//    // Jwt 토큰 생성
//    public String createToken(String userPk, List<String> roles) {
//        Claims claims = Jwts.claims().setSubject(userPk);
//        claims.put("roles", roles);
//        claims.put("userPk", userPk);
//        Date now = new Date();
//        return Jwts.builder()
//                .setClaims(claims) // 데이터
//                .setIssuedAt(now) // 토큰 발행일자
//                .setExpiration(new Date(now.getTime() + validityInMilliseconds)) // set Expire Time
//                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화 알고리즘, secret값 세팅
//                .compact();
//    }
//
//    // Jwt 토큰으로 인증 정보를 조회
//    public Authentication getAuthentication(String token) {
//        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserPk(token));
//        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
//    }
//
//    // Jwt 토큰에서 회원 구별 정보 추출
//    public String getUserPk(String token) {
//        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
//    }
//
//    // Request의 Header에서 token 파싱 : "X-AUTH-TOKEN: jwt토큰"
//    public String resolveToken(HttpServletRequest req) {
//        return req.getHeader("X-AUTH-TOKEN");
//    }
//
//    // Jwt 토큰의 유효성 + 만료일자 확인
//    public boolean validateToken(String jwtToken) {
//        try {
//            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
//            return !claims.getBody().getExpiration().before(new Date());
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    public List<String> getUserRoles(String token) {
//        return (List<String>)Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().get("roles");
//    }
//}
package com.ssafy.api.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private String secretKey;
    private long validityInMilliseconds;
    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long validityInMilliseconds,
            UserDetailsService userDetailsService
    ) {
        this.secretKey = secretKey;
        this.validityInMilliseconds = validityInMilliseconds;
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    protected void init() {
        // secretKey 를 Base64 로 인코딩
        this.secretKey = Base64.getEncoder().encodeToString(this.secretKey.getBytes());
    }

    /** 1) JWT 토큰 생성 */
    public String createToken(String userPk, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(userPk);
        claims.put("roles", roles);
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)              // payload
                .setIssuedAt(now)               // 발행 시간
                .setExpiration(exp)             // 만료 시간
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /** 2) JWT 토큰으로부터 Authentication 객체 얻기 */
    public Authentication getAuthentication(String token) {
        String username = getUserPk(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities()
        );
    }

    /** 3) JWT 토큰에서 userPk(=username) 추출 */
    public String getUserPk(String token) {
        Claims body = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return body.getSubject();
    }

    /** 4) HTTP Header 에서 토큰 꺼내기 */
    public String resolveToken(HttpServletRequest req) {
        return req.getHeader("X-AUTH-TOKEN");
    }

    /** 5) 토큰 유효성 검사 (서명 + 만료시간 체크) */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /** 6) 토큰에서 권한 목록만 꺼내기 (필요시) */
    @SuppressWarnings("unchecked")
    public List<String> getUserRoles(String token) {
        Claims body = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return (List<String>) body.get("roles");
    }
}
