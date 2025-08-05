// src/main/java/com/minjeok4go/petplace/user/service/UserService.java
package com.minjeok4go.petplace.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.minjeok4go.petplace.auth.dto.TokenDto;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.dto.CheckDuplicateResponseDto;
import com.minjeok4go.petplace.user.dto.UserSignupRequestDto;
import com.minjeok4go.petplace.user.dto.VerificationData;
import com.minjeok4go.petplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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
     * 포트원 본인인증 결과 조회 및 검증
     */
    private VerificationData verifyAndGetUserData(String impUid) {
        try {
            log.info("본인인증 검증 시작: impUid={}", impUid);

            // 포트원 API 호출하여 인증 결과 조회
            JsonNode certificationInfo = portOneApiService.getCertificationInfo(impUid);

            // 디버깅을 위해 포트원에서 받은 전체 데이터 출력
            log.info("### 포트원 본인인증 응답 결과: {}", certificationInfo.toPrettyString());

            // 1. 인증 완료 여부 확인 (기존: "status", 변경: "certified")
            boolean isCertified = certificationInfo.get("certified").asBoolean();
            if (!isCertified) {
                throw new IllegalArgumentException("본인인증이 완료되지 않았습니다.");
            }

            // 2. 인증 시간 검증 (30분 이내)
            long certifiedAt = certificationInfo.get("certified_at").asLong();
            long currentTime = System.currentTimeMillis() / 1000;
            if (currentTime - certifiedAt > 1800) { // 30분 = 1800초
                throw new IllegalArgumentException("본인인증이 만료되었습니다. 다시 인증해주세요.");
            }

            // 3. 생년월일 데이터 파싱 (기존: "birth", 변경: "birthday")
            String birthString = certificationInfo.get("birthday").asText(); // "YYYY-MM-DD" 형식
            LocalDate birthDate = LocalDate.parse(birthString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // 4. 외국인 여부 파싱 (기존: 문자열 "Y", 변경: boolean)
            boolean isForeigner = certificationInfo.get("foreigner").asBoolean();

            // 최종 데이터 조립
            VerificationData result = VerificationData.builder()
                    .ci(certificationInfo.get("unique_key").asText())
                    .name(certificationInfo.get("name").asText())
                    .phone(certificationInfo.get("phone").asText())
                    .birthDate(birthDate)
                    .gender(certificationInfo.get("gender").asText())
                    .isForeigner(isForeigner)
                    .build();

            log.info("본인인증 데이터 추출 완료: name={}, phone={}", result.getName(), result.getPhone());
            return result;

        } catch (Exception e) {
            log.error("본인인증 검증 실패: impUid={}", impUid, e);
            // 클라이언트에게 조금 더 친절한 메시지를 전달하기 위해 원본 예외 메시지를 포함하지 않을 수 있습니다.
            throw new IllegalArgumentException("본인인증 검증에 실패했습니다. 관리자에게 문의하세요.");
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
                //kg에서 외국인 여부 받을 수도 있긴한데 일단 안받는다 생각하고 테스트 해보려고 함 (25.08.05)
//                .isForeigner(verificationData.getIsForeigner())
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
