package com.minjeok4go.petplace.auth.service;

import com.minjeok4go.petplace.auth.dto.SocialLoginRequest;
import com.minjeok4go.petplace.auth.dto.SocialLoginResponse;
import com.minjeok4go.petplace.auth.dto.SocialSignupRequest;
import com.minjeok4go.petplace.auth.dto.TokenDto;
import com.minjeok4go.petplace.auth.jwt.JwtTokenProvider;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.service.SocialUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SocialAuthService {

    private final SocialUserService socialUserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * 소셜 로그인 처리
     */
    public SocialLoginResponse processSocialLogin(SocialLoginRequest request) {
        try {
            String socialId = request.getUserInfo().getSocialId();

            // 1. 소셜 ID로 기존 사용자 확인
            Optional<User> existingUser = socialUserService.findBySocialId(socialId);

            if (existingUser.isPresent()) {
                // 기존 사용자 - 바로 로그인 처리
                return handleExistingUser(existingUser.get());
            } else {
                // 신규 사용자 - 임시 토큰 발급
                String tempToken = jwtTokenProvider.createTempToken(socialId, request.getProvider().name());
                return SocialLoginResponse.newUser(tempToken);
            }

        } catch (Exception e) {
            log.error("소셜 로그인 처리 중 오류 발생: provider={}, socialId={}",
                    request.getProvider(), request.getUserInfo().getSocialId(), e);
            return SocialLoginResponse.error("로그인 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 소셜 회원가입 처리 (본인인증 완료 후)
     */
    public TokenDto processSocialSignup(SocialSignupRequest request) {
        try {
            // 1. 소셜 사용자 생성 또는 계정 연동
            User user = socialUserService.signupSocialUser(request);

            // 2. JWT 토큰 생성
            String accessToken = jwtTokenProvider.createAccessToken(user.getId());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

            // 3. Refresh Token 저장
            refreshTokenService.saveOrUpdate(user.getId(), refreshToken);

            // 4. TokenDto 반환
            return TokenDto.of(
                    accessToken,
                    refreshToken,
                    user.getId(),
                    user.getUserName(),
                    user.getNickname(),
                    user.getUserImgSrc(),
                    user.getLevel(),
                    user.getDefaultPetId(),
                    user.getRegionId()
            );

        } catch (IllegalArgumentException e) {
            throw e; // 유효성 검사 오류는 그대로 전파
        } catch (Exception e) {
            log.error("소셜 회원가입 처리 중 오류 발생: provider={}", request.getProvider(), e);
            throw new RuntimeException("회원가입 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * CI 기반으로 연동 가능한 계정 확인
     */
    public SocialLoginResponse checkLinkableAccount(String ci, String socialId, String provider) {
        try {
            Optional<User> linkableUser = socialUserService.findLinkableUser(ci);

            if (linkableUser.isPresent()) {
                // 연동 가능한 기존 계정 존재
                String tempToken = jwtTokenProvider.createTempToken(socialId, provider);
                return SocialLoginResponse.linkableUser(linkableUser.get().getId(), tempToken);
            } else {
                // 연동 가능한 계정 없음 - 신규 가입 진행
                String tempToken = jwtTokenProvider.createTempToken(socialId, provider);
                return SocialLoginResponse.newUser(tempToken);
            }

        } catch (Exception e) {
            log.error("연동 가능한 계정 확인 중 오류 발생: ci={}", ci, e);
            return SocialLoginResponse.error("계정 확인 중 오류가 발생했습니다.");
        }
    }

    /**
     * 기존 사용자 로그인 처리
     */
    private SocialLoginResponse handleExistingUser(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // Refresh Token 저장
        refreshTokenService.saveOrUpdate(user.getId(), refreshToken);

        TokenDto tokenDto = TokenDto.of(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUserName(),
                user.getNickname(),
                user.getUserImgSrc(),
                user.getLevel(),
                user.getDefaultPetId(),
                user.getRegionId()
        );

        return SocialLoginResponse.existingUser(tokenDto);
    }

    public void validateTempToken(String tempToken, SocialSignupRequest request) {
        Map<String, Object> claims = jwtTokenProvider.getTempTokenClaims(tempToken);

        String tokenSocialId = (String) claims.get("socialId");
        String tokenProvider = (String) claims.get("provider");

        if (!tokenSocialId.equals(request.getUserInfo().getSocialId()) ||
                !tokenProvider.equals(request.getProvider().name())) {
            throw new IllegalArgumentException("임시 토큰 정보가 일치하지 않습니다.");
        }
    }

}
