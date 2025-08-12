package com.minjeok4go.petplace.auth.service;

import com.minjeok4go.petplace.auth.dto.SocialLoginRequest;
import com.minjeok4go.petplace.auth.dto.SocialLoginResponse;
import com.minjeok4go.petplace.auth.dto.SocialSignupRequest;
import com.minjeok4go.petplace.auth.dto.TokenDto;
import com.minjeok4go.petplace.auth.jwt.JwtTokenProvider;
import com.minjeok4go.petplace.user.dto.KakaoUserInfo;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.service.SocialUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SocialAuthService {

    private final SocialUserService socialUserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final KakaoApiService kakaoApiService; // 추가

    /**
     * 소셜 로그인 처리 (보안 강화 버전)
     * 이제 액세스 토큰으로 카카오 API를 직접 호출하여 검증합니다.
     */
    public SocialLoginResponse processSocialLogin(SocialLoginRequest request) {
        try {
            log.info("소셜 로그인 처리 시작 - provider: {}", request.getProvider());
            
            // 1. 액세스 토큰으로 카카오 API에서 실제 사용자 정보 가져오기
            KakaoUserInfo kakaoUserInfo = kakaoApiService.getUserInfo(request.getAccessToken());
            String socialId = kakaoUserInfo.getSocialId();

            // 2. 소셜 ID로 기존 사용자 확인
            Optional<User> existingUser = socialUserService.findBySocialId(socialId);

            if (existingUser.isPresent()) {
                // 기존 사용자 - 바로 로그인 처리
                log.info("기존 사용자 로그인 - socialId: {}", socialId);
                return handleExistingUser(existingUser.get());
            } else {
                // 신규 사용자 - 임시 토큰 발급 (사용자 정보 포함)
                log.info("신규 사용자 감지 - socialId: {}", socialId);
                String tempToken = jwtTokenProvider.createTempTokenWithUserInfo(
                    socialId, 
                    request.getProvider().name(),
                    kakaoUserInfo.getEmail(),
                    kakaoUserInfo.getNickname(),
                    kakaoUserInfo.getProfileImage()
                );
                return SocialLoginResponse.newUser(tempToken);
            }

        } catch (Exception e) {
            log.error("소셜 로그인 처리 중 오류 발생: provider={}", request.getProvider(), e);
            return SocialLoginResponse.error("소셜 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 소셜 회원가입 처리 (본인인증 완료 후)
     */
    public TokenDto processSocialSignup(SocialSignupRequest request) {
        try {
            log.info("소셜 회원가입 처리 시작 - provider: {}", request.getProvider());
            
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
                    user.getRegionId(),
                    user.getPhoneNumber()
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
                user.getRegionId(),
                user.getPhoneNumber()
        );

        return SocialLoginResponse.existingUser(tokenDto);
    }
}
