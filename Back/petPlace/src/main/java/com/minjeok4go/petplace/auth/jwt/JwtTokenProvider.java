package com.minjeok4go.petplace.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;

    // application.yml 에 정의한 값들을 가져옵니다.
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.expiration_time}") long accessTokenExpirationTime,
                            @Value("${jwt.refresh_expiration_time}") long refreshTokenExpirationTime) {
        byte[] keyBytes = secretKey.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    // Access Token 생성
    public String createAccessToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationTime);

        return Jwts.builder()
                .setSubject(userId) // 토큰 주체로 userId 사용
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "access") // 여기서 토큰 타입 구분
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    // Refresh Token 생성
    public String createRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationTime);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "refresh") // 토큰 타입 구분
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    // 기존 createToken 메서드는 createAccessToken으로 변경
    public String createToken(String userId) {
        return createAccessToken(userId);
    }

    // 토큰에서 사용자 ID 추출
    public String getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    // 토큰 타입 확인
    public String getTokenType(String token) {
        Claims claims = parseClaims(token);
        return claims.get("type", String.class);
    }


    // 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        String userId = claims.getSubject(); // 사용자 ID 추출

        log.info("토큰에서 추출한 사용자 ID: {}", userId);

        // UserDetails 생성 (username을 userId로 설정)
        UserDetails userDetails = new User(userId, "", List.of());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰 유효성 검증 (로깅 추가)
    public boolean validateToken(String token) {
        try {
            log.info("토큰 검증 시작: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            log.info("토큰 검증 성공");
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT 토큰 검증 중 예외 발생: {}", e.getMessage());
        }
        return false;
    }
    // Refresh Token만 유효성 검증 (만료되어도 클레임 추출 가능)
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = parseClaims(refreshToken);
            String tokenType = claims.get("type", String.class);
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            log.error("Refresh Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }


    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
    // Refresh Token 만료 시간 계산 (LocalDateTime 반환)
    public java.time.LocalDateTime getRefreshTokenExpiryDate() {
        return java.time.LocalDateTime.now().plusSeconds(refreshTokenExpirationTime / 1000);
    }
}