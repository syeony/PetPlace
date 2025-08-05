// src/main/java/com/minjeok4go/petplace/user/service/UserService.java
package com.minjeok4go.petplace.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.minjeok4go.petplace.auth.dto.TokenDto;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.dto.CheckDuplicateResponseDto;
import com.minjeok4go.petplace.user.dto.UserSignupRequestDto;
import com.minjeok4go.petplace.user.dto.VerificationData;
import com.minjeok4go.petplace.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PortOneApiService portOneApiService;

    /**
     * 본인인증 + 회원가입 통합 처리
     */
    public void signup(UserSignupRequestDto requestDto) {
        log.info("회원가입 시작: userName={}, impUid={}", requestDto.getUserName(), requestDto.getImpUid());

        // 1. 포트원에서 본인인증 결과 조회 및 검증
        VerificationData verificationData = verifyAndGetUserData(requestDto.getImpUid());

        // 2. 기본 유효성 검사
        validateSignupRequest(requestDto, verificationData);

        // 3. 사용자 생성 및 저장
        User user = createUser(requestDto, verificationData);
        userRepository.save(user);

        log.info("회원가입 완료: userName={}, ci={}", user.getUserName(), user.getCi());
    }

    /**
     * 포트원 본인인증 결과 조회 및 검증 (수정된 메서드)
     */
    private VerificationData verifyAndGetUserData(String impUid) {
        try {
            log.info("본인인증 검증 시작: impUid={}", impUid);

            // [수정] PortOneApiService가 'response' 노드만 반환하는 것으로 확인됨.
            // 따라서 반환된 노드를 바로 responseData로 사용하고, 외부 구조(code, message) 파싱 로직을 제거합니다.
            JsonNode responseData = portOneApiService.getCertificationInfo(impUid);
            log.info("포트원 API 응답 데이터: {}", responseData.toPrettyString());

            // [수정] 'response' 객체에 대한 null 체크는 불필요하므로 제거합니다.
            if (responseData.isMissingNode() || responseData.isNull()) {
                throw new IllegalArgumentException("포트원으로부터 유효한 응답 데이터를 받지 못했습니다.");
            }

            // 본인인증 성공 여부 확인 (certified 필드)
            boolean isCertified = responseData.path("certified").asBoolean(false);
            if (!isCertified) {
                throw new IllegalArgumentException("본인인증이 완료되지 않았습니다.");
            }

            // CI(unique_key) 추출 및 검증
            String ci = responseData.path("unique_key").asText(null);
            // 로그에서 unique_key가 빈 문자열("")로 오는 것을 확인. 이에 대한 처리 추가.
            if (ci == null || ci.trim().isEmpty()) {
                log.warn("CI(unique_key)가 비어있습니다. 전화번호로 임시 CI 생성");
                String phone = responseData.path("phone").asText();
                ci = "TEMP_CI_" + phone + "_" + System.currentTimeMillis();
            }

            // 인증 시간 검증 (예: 30분 이내)
            long certifiedAt = responseData.path("certified_at").asLong();
            long currentTime = System.currentTimeMillis() / 1000;
            if (currentTime - certifiedAt > 1800) { // 30분 = 1800초
                throw new IllegalArgumentException("본인인증 유효 시간이 초과되었습니다. 다시 시도해주세요.");
            }

            // 생년월일 파싱 방식 변경 (birthday 필드 사용)
            String birthdayString = responseData.path("birthday").asText(null);
            if (birthdayString == null) {
                throw new IllegalArgumentException("생년월일 정보가 없습니다.");
            }
            LocalDate birthDate = LocalDate.parse(birthdayString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            VerificationData result = VerificationData.builder()
                    .ci(ci)
                    .name(responseData.path("name").asText())
                    .phone(responseData.path("phone").asText())
                    .birthDate(birthDate)
                    .gender(responseData.path("gender").asText())
                    .isForeigner(responseData.path("foreigner").asBoolean(false))
                    .build();

            log.info("본인인증 데이터 추출 완료: name={}, phone={}", result.getName(), result.getPhone());
            return result;

        } catch (Exception e) {
            log.error("본인인증 검증 실패: impUid={}, 오류타입: {}, 메시지: {}", impUid, e.getClass().getSimpleName(), e.getMessage(), e);
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("본인인증 검증 중 시스템 오류가 발생했습니다.", e);
        }
    }

    /**
     * 회원가입 유효성 검사
     */
    private void validateSignupRequest(UserSignupRequestDto requestDto, VerificationData verificationData) {
        // 아이디 중복 확인
        if (userRepository.existsByUserName(requestDto.getUserName())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // CI 중복 확인 (가장 중요!)
        if (userRepository.existsByCi(verificationData.getCi())) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        }

        // 전화번호 중복 확인 (추가 보안)
        if (userRepository.existsByPhoneNumber(verificationData.getPhone())) {
            throw new IllegalArgumentException("이미 가입된 전화번호입니다.");
        }
    }

    /**
     * User 엔티티 생성
     */
    private User createUser(UserSignupRequestDto requestDto, VerificationData verificationData) {
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        return User.builder()
                .userName(requestDto.getUserName())
                .password(encodedPassword)
                .name(verificationData.getName())
                .nickname(requestDto.getNickname())
                .regionId(requestDto.getRegionId() != null ? requestDto.getRegionId() : 1L)
                .ci(verificationData.getCi())
                .phoneNumber(verificationData.getPhone())
                .gender(verificationData.getGender())
                .birthday(verificationData.getBirthDate())
                .build();
    }

    // 아이디 중복 체크
    @Transactional(readOnly = true)
    public CheckDuplicateResponseDto checkUserNameDuplicate(String userName) {
        boolean isDuplicate = userRepository.existsByUserName(userName);
        String message = isDuplicate ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.";
        return new CheckDuplicateResponseDto(isDuplicate, message);
    }

    // 닉네임 중복 체크
    @Transactional(readOnly = true)
    public CheckDuplicateResponseDto checkNicknameDuplicate(String nickname) {
        boolean isDuplicate = userRepository.existsByNickname(nickname);
        String message = isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.";
        return new CheckDuplicateResponseDto(isDuplicate, message);
    }

    // AuthService에서 호출할 사용자 조회 메서드
    @Transactional(readOnly = true)
    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디입니다."));
    }

    public User getUserFromToken(TokenDto.UserInfo tokenUser) {
        return userRepository.findByUserName(tokenUser.getUserName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + tokenUser.getUserName()));
    }
}
