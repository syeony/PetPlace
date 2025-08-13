package com.minjeok4go.petplace.user.service;

import lombok.RequiredArgsConstructor;
import com.minjeok4go.petplace.auth.dto.SocialSignupRequest;
import com.minjeok4go.petplace.auth.jwt.JwtTokenProvider;
import com.minjeok4go.petplace.common.constant.SocialProvider;
import com.minjeok4go.petplace.feed.service.FeedService;
import com.minjeok4go.petplace.region.entity.Region;
import com.minjeok4go.petplace.user.dto.VerificationData;
import com.minjeok4go.petplace.user.entity.LoginType;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.minjeok4go.petplace.region.repository.RegionRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SocialUserService {

    private final UserRepository userRepository;
    private final PortOneApiService portOneApiService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RegionRepository regionRepository;


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
     * 소셜 회원가입 처리 (보안 강화 버전)
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
     * 기존 계정에 소셜 계정 연동 (수정됨)
     */
    private User linkSocialAccount(SocialSignupRequest request, VerificationData verificationData) {
        // 1. 임시 토큰에서 검증된 사용자 정보 추출
        Map<String, Object> tempTokenClaims = jwtTokenProvider.getTempTokenClaims(request.getTempToken());
        String socialId = (String) tempTokenClaims.get("socialId");
        String socialEmail = (String) tempTokenClaims.get("email");
        String profileImage = (String) tempTokenClaims.get("profileImage");
        
        // 2. 연동할 기존 사용자 조회
        User existingUser = userRepository.findById(request.getLinkUserId())
                .orElseThrow(() -> new IllegalArgumentException("연동할 사용자를 찾을 수 없습니다."));

        // 3. CI 일치 여부 확인 (보안)
        if (!existingUser.getCi().equals(verificationData.getCi())) {
            throw new IllegalArgumentException("본인인증 정보가 일치하지 않습니다.");
        }

        // 4. 소셜 계정 연동
        LoginType loginType = convertProviderToLoginType(request.getProvider());
        existingUser.linkSocialAccount(
                socialId,       // 임시토큰에서 추출한 검증된 socialId
                socialEmail,    // 임시토큰에서 추출한 검증된 email
                profileImage,   // 임시토큰에서 추출한 검증된 profileImage
                loginType
        );

        log.info("소셜 계정 연동 완료: userId={}, provider={}, socialId={}", 
                existingUser.getId(), request.getProvider(), socialId);
        return existingUser;
    }



    /**
     * 새로운 소셜 사용자 생성 (보안 강화 버전)
     */
    private User createSocialUser(SocialSignupRequest request, VerificationData verificationData) {
        // 1. 임시 토큰에서 검증된 사용자 정보 추출
        Map<String, Object> tempTokenClaims = jwtTokenProvider.getTempTokenClaims(request.getTempToken());
        String socialId = (String) tempTokenClaims.get("socialId");
        String socialEmail = (String) tempTokenClaims.get("email");
        String profileImage = (String) tempTokenClaims.get("profileImage");

        // 2. 중복 검사
        validateSocialSignup(socialId, request.getNickname(), verificationData);

        // 3. 소셜 사용자용 랜덤 패스워드 생성
        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(randomPassword);

        // ✅ 4. DTO에서 받은 regionId로 실제 Region 엔티티 조회
        Long regionId = request.getRegionId();
        if (regionId == null) {
            // 소셜 회원가입 시 지역 선택이 필수인 경우
            throw new IllegalArgumentException("지역 ID는 필수 입력값입니다.");
        }
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역 ID입니다: " + regionId));

        // 5. 소셜 사용자 생성
        LoginType loginType = convertProviderToLoginType(request.getProvider());

        // ✅ 6. User.createSocialUser() 팩토리 메소드에 조회한 Region 객체를 전달
        User socialUser = User.createSocialUser(
                verificationData.getName(),
                request.getNickname(),
                region,
                verificationData.getCi(),
                verificationData.getPhone(),
                verificationData.getGender(),
                verificationData.getBirthDate(),
                socialId,         // 임시토큰에서 추출한 검증된 socialId
                socialEmail,      // 임시토큰에서 추출한 검증된 email
                profileImage,     // 임시토큰에서 추출한 검증된 profileImage
                loginType,
                encodedPassword   // 인코딩된 랜덤 패스워드
        );

        User savedUser = userRepository.save(socialUser);
        log.info("소셜 사용자 생성 완료: userId={}, provider={}, socialId={}",
                savedUser.getId(), request.getProvider(), socialId);

        return savedUser;
    }

    /**
     * 소셜 회원가입 유효성 검사 (수정됨)
     */
    private void validateSocialSignup(String socialId, String nickname, VerificationData verificationData) {
        // 1. CI 중복 확인 (가장 중요!)
        if (userRepository.existsByCi(verificationData.getCi())) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        }

        // 2. 소셜 ID 중복 확인
        if (userRepository.existsBySocialId(socialId)) {
            throw new IllegalArgumentException("이미 가입된 소셜 계정입니다.");
        }

        // 3. 닉네임 중복 확인
        if (userRepository.existsByNickname(nickname)) {
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
