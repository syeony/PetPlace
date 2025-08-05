package com.minjeok4go.petplace.auth.jwt;

import com.minjeok4go.petplace.auth.dto.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpiryInMs;
    private final long refreshTokenExpiryInMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry}") long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry) {

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryInMs = accessTokenExpiry;
        this.refreshTokenExpiryInMs = refreshTokenExpiry;
    }

    // Access Token 생성
    public String createAccessToken(String userName) {
        return Jwts.builder()
                .setSubject(userName)
                .claim("type", TokenType.ACCESS.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiryInMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String userName) {
        return Jwts.builder()
                .setSubject(userName)
                .claim("type", TokenType.REFRESH.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiryInMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // 토큰에서 사용자명 추출
    public String getUserNameFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            String userName = claims.getSubject();
            log.debug("토큰에서 추출한 사용자명: {}", userName);
            return userName;
        } catch (Exception e) {
            log.error("토큰에서 사용자명 추출 실패: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }

    // 하위 호환성을 위한 메서드
    public String getUserIdFromToken(String token) {
        return getUserNameFromToken(token);
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            log.debug("토큰 검증 성공");
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.debug("만료된 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.debug("지원되지 않는 토큰: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("잘못된 형식의 토큰: {}", e.getMessage());
        } catch (SecurityException e) {
            log.debug("유효하지 않은 서명: {}", e.getMessage());
        } catch (Exception e) {
            log.debug("토큰 검증 실패: {}", e.getMessage());
        }
        return false;
    }

    // Refresh Token 검증
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = parseClaims(refreshToken);
            String tokenType = claims.get("type", String.class);

            boolean isRefreshToken = TokenType.REFRESH.toString().equals(tokenType);
            boolean isNotExpired = !claims.getExpiration().before(new Date());

            log.debug("Refresh Token 검증 결과 - 타입: {}, 만료여부: {}", tokenType, !isNotExpired);
            return isRefreshToken && isNotExpired;

        } catch (Exception e) {
            log.debug("Refresh Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // Refresh Token 만료일 반환
    public LocalDateTime getRefreshTokenExpiryDate() {
        return LocalDateTime.now().plusSeconds(refreshTokenExpiryInMs / 1000);
    }

    // HTTP 요청에서 토큰 추출
    public String resolveToken(jakarta.servlet.http.HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Claims 파싱
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}