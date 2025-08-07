package com.minjeok4go.petplace.auth.jwt;

import com.minjeok4go.petplace.auth.dto.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    //  Access Token 생성 - userId(Long)를 받아서 토큰에 저장
    public String createAccessToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())  // Long을 String으로 변환하여 저장
                .claim("type", TokenType.ACCESS.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiryInMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    //  Refresh Token 생성 - userId(Long)를 받아서 토큰에 저장
    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())  // Long을 String으로 변환하여 저장
                .claim("type", TokenType.REFRESH.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiryInMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // ✅ 누락된 getAuthentication 메서드 추가
    public Authentication getAuthentication(String token) {
        try {
            Long userId = getUserIdFromToken(token);
            log.debug("토큰에서 추출한 사용자 ID로 Authentication 생성: {}", userId);

            // 간단한 Authentication 객체 생성
            // principal에는 userId를 문자열로 저장 (컨트롤러에서 @AuthenticationPrincipal로 받을 수 있도록)
            return new UsernamePasswordAuthenticationToken(
                    userId.toString(),  // principal (컨트롤러에서 @AuthenticationPrincipal로 받는 값)
                    null,               // credentials (비밀번호는 필요없음)
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))  // authorities
            );
        } catch (Exception e) {
            log.error("토큰으로부터 Authentication 생성 실패: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.", e);
        }
    }

    // 토큰에서 사용자 ID 추출 - Long 타입으로 반환
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            String userIdStr = claims.getSubject();
            log.debug("토큰에서 추출한 사용자 ID: {}", userIdStr);
            return Long.parseLong(userIdStr);  // String을 Long으로 변환
        } catch (NumberFormatException e) {
            log.error("토큰의 사용자 ID 형식이 잘못됨: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 토큰 형식입니다.");
        } catch (Exception e) {
            log.error("토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }

    //  하위 호환성을 위한 메서드
    @Deprecated
    public String getUserNameFromToken(String token) {
        return getUserIdFromToken(token).toString();
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

    /**
     * 소셜 로그인용 임시 토큰 생성 (15분 유효)
     * @param socialId 소셜 플랫폼 고유 ID
     * @param provider 소셜 플랫폼명
     * @return 임시 토큰
     */
    public String createTempToken(String socialId, String provider) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 900000); // 15분

        return Jwts.builder()
                .setSubject("TEMP_" + provider + "_" + socialId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "TEMP_SOCIAL")
                .claim("provider", provider)
                .claim("socialId", socialId)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 사용자 정보가 포함된 소셜 로그인용 임시 토큰 생성 (15분 유효)
     * @param socialId 소셜 플랫폼 고유 ID
     * @param provider 소셜 플랫폼명
     * @param email 사용자 이메일
     * @param nickname 사용자 닉네임
     * @param profileImage 프로필 이미지 URL
     * @return 사용자 정보가 포함된 임시 토큰
     */
    public String createTempTokenWithUserInfo(String socialId, String provider, String email, String nickname, String profileImage) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 900000); // 15분

        return Jwts.builder()
                .setSubject("TEMP_" + provider + "_" + socialId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "TEMP_SOCIAL_WITH_INFO")
                .claim("provider", provider)
                .claim("socialId", socialId)
                .claim("email", email)
                .claim("nickname", nickname)
                .claim("profileImage", profileImage)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 임시 토큰에서 소셜 정보 추출
     */
    public Map<String, Object> getTempTokenClaims(String tempToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(tempToken)
                    .getBody();

            String tokenType = (String) claims.get("type"); // 토큰의 타입을 가져옴
            if (!"TEMP_SOCIAL".equals(tokenType) && !"TEMP_SOCIAL_WITH_INFO".equals(tokenType)) {
                // "TEMP_SOCIAL"도 아니고, "TEMP_SOCIAL_WITH_INFO"도 아니면 에러 발생
                throw new IllegalArgumentException("유효하지 않은 임시 토큰입니다.");
            }


            Map<String, Object> result = new HashMap<>();
            result.put("provider", claims.get("provider"));
            result.put("socialId", claims.get("socialId"));
            return result;

        } catch (Exception e) {
            throw new IllegalArgumentException("임시 토큰이 유효하지 않습니다.", e);
        }
    }

    /**
     * 토큰의 남은 유효시간을 초 단위로 반환
     */
    public Long getTokenExpiryInSeconds(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();

            long expiryTime = (expiration.getTime() - now.getTime()) / 1000;
            return Math.max(0, expiryTime); // 음수가 나올 경우 0 반환
        } catch (Exception e) {
            return 0L; // 토큰이 유효하지 않으면 0 반환
        }
    }

    /**
     * 토큰 유효성 검증과 함께 상세 정보 반환
     */
    public TokenValidationResult validateTokenWithDetails(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();

            boolean isExpired = expiration.before(now);
            if (isExpired) {
                return new TokenValidationResult(false, "토큰이 만료되었습니다", null, 0L);
            }

            String userIdStr = claims.getSubject();
            Long userId = Long.parseLong(userIdStr);
            Long expiresIn = (expiration.getTime() - now.getTime()) / 1000;

            return new TokenValidationResult(true, "토큰이 유효합니다", userId, expiresIn);

        } catch (ExpiredJwtException e) {
            return new TokenValidationResult(false, "토큰이 만료되었습니다", null, 0L);
        } catch (UnsupportedJwtException e) {
            return new TokenValidationResult(false, "지원되지 않는 토큰 형식입니다", null, 0L);
        } catch (MalformedJwtException e) {
            return new TokenValidationResult(false, "잘못된 형식의 토큰입니다", null, 0L);
        } catch (SecurityException e) {
            return new TokenValidationResult(false, "토큰 서명이 유효하지 않습니다", null, 0L);
        } catch (NumberFormatException e) {
            return new TokenValidationResult(false, "토큰의 사용자 ID 형식이 올바르지 않습니다", null, 0L);
        } catch (Exception e) {
            return new TokenValidationResult(false, "토큰 검증 중 오류가 발생했습니다", null, 0L);
        }
    }

    // TokenValidationResult 내부 클래스 추가
    @Getter
    @AllArgsConstructor
    public static class TokenValidationResult {
        private boolean valid;
        private String message;
        private Long userId;
        private Long expiresIn;
    }
}