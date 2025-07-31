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

    // Refresh Token 저장 또는 업데이트
    public RefreshToken saveOrUpdate(String userId, String refreshToken) {
        LocalDateTime expiresAt = jwtTokenProvider.getRefreshTokenExpiryDate();

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(userId);

        if (existingToken.isPresent()) {
            // 기존 토큰 업데이트
            existingToken.get().updateToken(refreshToken, expiresAt);
            return existingToken.get();
        } else {
            // 새 토큰 저장
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .userId(userId)
                    .refreshToken(refreshToken)
                    .expiresAt(expiresAt)
                    .build();
            return refreshTokenRepository.save(newRefreshToken);
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

    // 사용자의 모든 Refresh Token 삭제 (로그아웃)
    public void deleteByUserId(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    // 특정 Refresh Token 삭제
    public void deleteByRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }

    public TokenRefreshResponseDto refreshToken(String refreshToken) {
        // 1. Refresh Token 유효성 검증
        if (!validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. Refresh Token으로 사용자 조회
        RefreshToken token = findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token을 찾을 수 없습니다."));

        String userId = token.getUserId();

        // 3. 새로운 Access Token과 Refresh Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        // 4. 새로운 Refresh Token 저장
        saveOrUpdate(userId, newRefreshToken);

        // 5. 기존 Refresh Token 삭제
        deleteByRefreshToken(refreshToken);

        return TokenRefreshResponseDto.success(newAccessToken, newRefreshToken);
    }
}
