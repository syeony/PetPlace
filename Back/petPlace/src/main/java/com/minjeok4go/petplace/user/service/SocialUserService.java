package com.minjeok4go.petplace.user.service;

import com.minjeok4go.petplace.auth.dto.SocialSignupRequest;
import com.minjeok4go.petplace.common.constant.SocialProvider;
import com.minjeok4go.petplace.user.dto.VerificationData;
import com.minjeok4go.petplace.user.entity.LoginType;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SocialUserService {

    private final UserRepository userRepository;
    private final PortOneApiService portOneApiService;
    private final PasswordEncoder passwordEncoder; // 추가

    /**
     * 소셜 ID로 기존 사용자 조회
     */
    @Transactional(readOnly = true)
    public Optional<User> findBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId);
    }

    /**
     * CI로 연동 가능한 기존 계정 찾기 (수정됨)
     */
    @Transactional(readOnly = true)
    public Optional<User> findLinkableUser(String ci) {
        List<User> users = userRepository.findAllByCi(ci);

        // 일반 이메일 가입자 중 소셜 계정이 연동되지 않은 사용자 찾기
        return users.stream()
                .filter(user -> LoginType.EMAIL.equals(user.getLoginType()) && !user.hasSocialAccount())
                .findFirst();
    }

    /**
     * 소셜 회원가입 처리
     */
    public User signupSocialUser(SocialSignupRequest request) {
        // 1. 본인인증 정보 조회
        VerificationData verificationData = getVerificationData(request.getImpUid());

        // 2. 연동 모드인지 신규 가입인지 확인
        if (request.getLinkUserId() != null) {
            return linkSocialAccount(request, verificationData);
        } else {
            return createSocialUser(request, verificationData);
        }
    }

    /**
     * 기존 계정에 소셜 계정 연동
     */
    private User linkSocialAccount(SocialSignupRequest request, VerificationData verificationData) {
        // 1. 연동할 기존 사용자 조회
        User existingUser = userRepository.findById(request.getLinkUserId())
                .orElseThrow(() -> new IllegalArgumentException("연동할 사용자를 찾을 수 없습니다."));

        // 2. CI 일치 여부 확인 (보안)
        if (!existingUser.getCi().equals(verificationData.getCi())) {
            throw new IllegalArgumentException("본인인증 정보가 일치하지 않습니다.");
        }

        // 3. 소셜 계정 연동
        LoginType loginType = convertProviderToLoginType(request.getProvider());
        existingUser.linkSocialAccount(
                request.getUserInfo().getSocialId(),
                request.getUserInfo().getEmail(),
                request.getUserInfo().getProfileImage(),
                loginType
        );

        log.info("소셜 계정 연동 완료: userId={}, provider={}", existingUser.getId(), request.getProvider());
        return existingUser;
    }

    /**
     * 새로운 소셜 사용자 생성
     */
    private User createSocialUser(SocialSignupRequest request, VerificationData verificationData) {
        // 1. 중복 검사
        validateSocialSignup(request, verificationData);

        // 2. 소셜 사용자용 랜덤 패스워드 생성
        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(randomPassword);
        
        // 3. 소셜 사용자 생성
        LoginType loginType = convertProviderToLoginType(request.getProvider());

        User socialUser = User.createSocialUser(
                verificationData.getName(),
                request.getNickname(),
                request.getRegionId(),
                verificationData.getCi(),
                verificationData.getPhone(),
                verificationData.getGender(),
                verificationData.getBirthDate(),
                request.getUserInfo().getSocialId(),
                request.getUserInfo().getEmail(),
                request.getUserInfo().getProfileImage(),
                loginType,
                encodedPassword // 인코딩된 랜덤 패스워드 추가
        );

        User savedUser = userRepository.save(socialUser);
        log.info("소셜 사용자 생성 완료: userId={}, provider={}", savedUser.getId(), request.getProvider());

        return savedUser;
    }

    /**
     * 소셜 회원가입 유효성 검사
     */
    private void validateSocialSignup(SocialSignupRequest request, VerificationData verificationData) {
        // 1. CI 중복 확인 (가장 중요!)
        if (userRepository.existsByCi(verificationData.getCi())) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        }

        // 2. 소셜 ID 중복 확인
        if (userRepository.existsBySocialId(request.getUserInfo().getSocialId())) {
            throw new IllegalArgumentException("이미 가입된 소셜 계정입니다.");
        }

        // 3. 자동 생성될 userName 중복 확인
        String autoUserName = request.getProvider().createUserName(request.getUserInfo().getSocialId());
        if (userRepository.existsByUserName(autoUserName)) {
            throw new IllegalArgumentException("시스템 오류가 발생했습니다. 다시 시도해주세요.");
        }

        // 4. 닉네임 중복 확인
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
    }

    /**
     * 본인인증 데이터 조회 (기존 UserService 로직 재사용)
     */
    private VerificationData getVerificationData(String impUid) {
        try {
            return portOneApiService.getVerificationData(impUid);
        } catch (Exception e) {
            log.error("본인인증 정보 조회 실패: impUid={}", impUid, e);
            throw new RuntimeException("본인인증 정보를 조회할 수 없습니다.", e);
        }
    }

    /**
     * SocialProvider를 LoginType으로 변환
     */
    private LoginType convertProviderToLoginType(SocialProvider provider) {
        return switch (provider) {
            case KAKAO -> LoginType.KAKAO;
            case NAVER -> LoginType.NAVER;
            case GOOGLE -> LoginType.GOOGLE;
        };
    }
}
