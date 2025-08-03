package com.minjeok4go.petplace.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import com.minjeok4go.petplace.auth.dto.TokenType;  // 🆕 추가

import java.security.Key;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.expiration_time}") long accessTokenExpirationTime,
                            @Value("${jwt.refresh_expiration_time}") long refreshTokenExpirationTime) {
        byte[] keyBytes = secretKey.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    // ✅ Access Token 생성 (Enum 적용)
    public String createAccessToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationTime);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", TokenType.ACCESS.toString())  // ✅ Enum 사용
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Refresh Token 생성 (Enum 적용)
    public String createRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationTime);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", TokenType.REFRESH.toString())  // ✅ Enum 사용
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createToken(String userId) {
        return createAccessToken(userId);
    }

    public String getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    public String getTokenType(String token) {
        Claims claims = parseClaims(token);
        return claims.get("type", String.class);
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        String userId = claims.getSubject();

        log.debug("토큰에서 추출한 사용자 ID: {}", userId);

        UserDetails userDetails = new User(userId, "", List.of());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            log.debug("토큰 검증 시작");
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            log.debug("토큰 검증 성공");
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

    // ✅ Refresh Token 검증 (Enum 적용)
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = parseClaims(refreshToken);
            String tokenType = claims.get("type", String.class);
            return TokenType.REFRESH.toString().equals(tokenType);  // ✅ Enum 사용
        } catch (Exception e) {
            log.error("Refresh Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // 🆕 새로운 메서드: 타입 안전 검증
    public boolean validateTokenType(String token, TokenType expectedType) {
        try {
            Claims claims = parseClaims(token);
            String tokenType = claims.get("type", String.class);
            TokenType actualType = TokenType.fromString(tokenType);
            return expectedType.equals(actualType);
        } catch (Exception e) {
            log.error("토큰 타입 검증 실패: {}", e.getMessage());
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

    public java.time.LocalDateTime getRefreshTokenExpiryDate() {
        return java.time.LocalDateTime.now().plusSeconds(refreshTokenExpirationTime / 1000);
    }
}