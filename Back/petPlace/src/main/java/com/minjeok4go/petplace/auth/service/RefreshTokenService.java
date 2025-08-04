package com.minjeok4go.petplace.auth.service;

import com.minjeok4go.petplace.auth.domain.RefreshToken;
import com.minjeok4go.petplace.auth.dto.TokenRefreshResponseDto;
import com.minjeok4go.petplace.auth.jwt.JwtTokenProvider;
import com.minjeok4go.petplace.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // Refresh Token 저장 또는 업데이트 (userId는 실제로 userName을 저장)
    public void saveOrUpdate(String userId, String refreshToken) {
        LocalDateTime expiresAt = jwtTokenProvider.getRefreshTokenExpiryDate();

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(userId);

        if (existingToken.isPresent()) {
            // 기존 토큰 업데이트
            existingToken.get().updateToken(refreshToken, expiresAt);
            existingToken.get();
        } else {
            // 새 토큰 저장
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .userId(userId)
                    .refreshToken(refreshToken)
                    .expiresAt(expiresAt)
                    .build();
            refreshTokenRepository.save(newRefreshToken);
        }
    }

    // Refresh Token으로 조회
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken);
    }

    // Refresh Token 유효성 검증
    @Transactional(readOnly = true)
    public boolean validateRefreshToken(String refreshToken) {
        Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByRefreshToken(refreshToken);

        if (tokenOptional.isEmpty()) {
            log.warn("데이터베이스에서 Refresh Token을 찾을 수 없음");
            return false;
        }

        RefreshToken token = tokenOptional.get();
        if (token.isExpired()) {
            log.warn("Refresh Token이 만료됨: {}", refreshToken);
            // 만료된 토큰 삭제
            refreshTokenRepository.delete(token);
            return false;
        }

        return jwtTokenProvider.validateRefreshToken(refreshToken);
    }

    // 사용자의 모든 Refresh Token 삭제 (로그아웃) - userId는 실제로 userName
    public void deleteByUserId(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    // 특정 Refresh Token 삭제
    public void deleteByRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }

    // ✅ 강화된 토큰 갱신 메서드
    public TokenRefreshResponseDto refreshToken(String refreshToken) {
        log.info("토큰 갱신 요청 시작");

        // 1. 원자적 토큰 검증 및 즉시 삭제 (Race Condition 방지)
        RefreshToken token = findAndDeleteRefreshToken(refreshToken);

        if (token == null) {
            log.warn("존재하지 않는 또는 이미 사용된 Refresh Token 사용 시도");
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. 만료 검증
        if (token.isExpired()) {
            log.warn("만료된 Refresh Token 사용 시도 - UserId: {}", token.getUserId());
            throw new IllegalArgumentException("만료된 Refresh Token입니다.");
        }

        // 3. JWT 토큰 자체 유효성 검증
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.warn("JWT 검증 실패한 Refresh Token - UserId: {}", token.getUserId());
            // 보안 위협 감지: 해당 사용자의 모든 토큰 무효화
            deleteByUserId(token.getUserId());
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다. 보안을 위해 모든 세션이 종료되었습니다.");
        }

        String userId = token.getUserId();
        log.info("토큰 갱신 진행 - UserId: {}", userId);

        try {
            // 4. 새로운 토큰 쌍 생성
            String newAccessToken = jwtTokenProvider.createAccessToken(userId);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

            // 5. 새로운 Refresh Token 저장
            saveOrUpdate(userId, newRefreshToken);

            log.info("토큰 갱신 성공 - UserId: {}", userId);
            return TokenRefreshResponseDto.success(newAccessToken, newRefreshToken);

        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생 - UserId: {}, Error: {}", userId, e.getMessage());
            // 오류 발생 시 보안을 위해 해당 사용자의 모든 토큰 삭제
            deleteByUserId(userId);
            throw new RuntimeException("토큰 갱신 중 오류가 발생했습니다. 다시 로그인해주세요.", e);
        }
    }

    // 원자적 토큰 조회 및 삭제
    @Transactional
    protected RefreshToken findAndDeleteRefreshToken(String refreshToken) {
        Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByRefreshToken(refreshToken);

        if (tokenOptional.isPresent()) {
            RefreshToken token = tokenOptional.get();
            // 즉시 삭제로 재사용 방지 (토큰 로테이션의 핵심)
            refreshTokenRepository.delete(token);
            log.debug("Refresh Token 사용 후 즉시 삭제 완료");
            return token;
        }

        return null;
    }
}
