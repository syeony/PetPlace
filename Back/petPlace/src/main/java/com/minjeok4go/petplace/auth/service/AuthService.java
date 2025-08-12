package com.minjeok4go.petplace.auth.service;

import com.minjeok4go.petplace.auth.dto.TokenDto;
import com.minjeok4go.petplace.auth.dto.TokenValidationResponse;
import com.minjeok4go.petplace.auth.jwt.JwtTokenProvider;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.dto.UserLoginRequestDto;
import com.minjeok4go.petplace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * 로그인 처리
     */
    public TokenDto login(UserLoginRequestDto requestDto) {
        // 1. 아이디로 사용자 조회 (UserService 위임)
        User user = userService.findByUserName(requestDto.getUserName());

        // 2. 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. Access Token과 Refresh Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 4. Refresh Token 저장
        refreshTokenService.saveOrUpdate(user.getId(), refreshToken);

        // 5. 토큰 및 사용자 정보 반환
        return TokenDto.of(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUserName(),
                user.getNickname(),
                user.getUserImgSrc(),
                user.getLevel(),
                user.getDefaultPetId(),
                user.getRegionId(),
                user.getPhoneNumber()
        );
    }

    /**
     * Access Token 유효성 검증
     */
    public TokenValidationResponse validateAccessToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return TokenValidationResponse.invalid("토큰이 제공되지 않았습니다");
        }

        JwtTokenProvider.TokenValidationResult result = jwtTokenProvider.validateTokenWithDetails(token);

        if (result.isValid()) {
            // 실제 사용자가 존재하는지 확인
            try {
                User user = userService.getUserById(result.getUserId());
                return TokenValidationResponse.valid(user.getId(), result.getExpiresIn());
            } catch (Exception e) {
                return TokenValidationResponse.invalid("토큰의 사용자 정보를 찾을 수 없습니다");
            }
        } else {
            return TokenValidationResponse.invalid(result.getMessage());
        }
    }

    /**
     * 토큰에서 추출한 사용자 ID로 사용자 조회
     * 컨트롤러에서 @AuthenticationPrincipal로 받은 String 타입 사용자 ID 처리
     *
     * @param tokenUserId 토큰에서 추출한 사용자 ID (문자열 형태)
     * @return User 엔티티
     * @throws UsernameNotFoundException 사용자를 찾을 수 없거나 ID 형식이 잘못된 경우
     */
    @Transactional(readOnly = true)
    public User getUserFromToken(String tokenUserId) {
        log.debug("토큰에서 사용자 조회 시작: tokenUserId={}", tokenUserId);

        if (tokenUserId == null || tokenUserId.trim().isEmpty()) {
            throw new UsernameNotFoundException("토큰에서 사용자 ID를 추출할 수 없습니다.");
        }

        try {
            Long userId = Long.parseLong(tokenUserId);
            User user = userService.getUserById(userId);
            log.debug("토큰에서 사용자 조회 완료: userId={}, userName={}", user.getId(), user.getUserName());
            return user;
        } catch (NumberFormatException e) {
            log.error("토큰의 사용자 ID 형식이 잘못되었습니다: tokenUserId={}", tokenUserId, e);
            throw new UsernameNotFoundException("토큰의 사용자 ID 형식이 잘못되었습니다: " + tokenUserId);
        }
    }

    /**
     * 컨트롤러에서 사용하기 쉬운 별칭 메서드
     * 기존 컨트롤러 코드와의 호환성을 위해 제공
     */
    @Transactional(readOnly = true)
    public User getUserByStringId(String tokenUserId) {
        return getUserFromToken(tokenUserId);
    }
}