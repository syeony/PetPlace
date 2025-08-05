// src/main/java/com/minjeok4go/petplace/user/service/UserService.java
package com.minjeok4go.petplace.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.dto.CheckDuplicateResponseDto;
import com.minjeok4go.petplace.user.dto.UserSignupRequestDto;
import com.minjeok4go.petplace.user.dto.VerificationData;
import com.minjeok4go.petplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            JsonNode certificationInfo = null;

            try {
                certificationInfo = portOneApiService.getCertificationInfo(impUid);
                log.info("포트원 API 호출 완료, 응답 null 여부: {}", certificationInfo == null);
            } catch (Exception apiException) {
                log.error("포트원 API 호출 중 예외 발생: {}", apiException.getMessage(), apiException);
                throw new IllegalArgumentException("포트원 API 호출 실패: " + apiException.getMessage());
            }

            // ✅ Null 체크 (가장 중요!)
            if (certificationInfo == null) {
                log.error("포트원 API 응답이 null입니다. impUid={}", impUid);
                throw new IllegalArgumentException("포트원 인증 정보를 가져올 수 없습니다. API 키를 확인해주세요.");
            }

            // 응답 내용 로깅
            log.info("포트원 API 응답 내용: {}", certificationInfo.toPrettyString());

            // 인증 상태 확인
            JsonNode statusNode = certificationInfo.get("status");
            if (statusNode == null) {
                log.error("status 필드가 없습니다. 전체 응답: {}", certificationInfo.toPrettyString());
                throw new IllegalArgumentException("잘못된 포트원 API 응답 형식입니다.");
            }

            String status = statusNode.asText();
            log.info("본인인증 상태: status={}", status);

            if (!"verified".equals(status)) {
                throw new IllegalArgumentException("본인인증이 완료되지 않았습니다. 상태: " + status);
            }
            // ✅ CI(unique_key) 검증 추가
            String ci = certificationInfo.get("unique_key").asText();
            if (ci == null || ci.trim().isEmpty()) {
                // CI가 없는 경우 대체 방안
                log.warn("CI(unique_key)가 비어있습니다. 전화번호로 중복 체크 진행");
                ci = "TEMP_CI_" + certificationInfo.get("phone").asText() + "_" + System.currentTimeMillis();
            }


            // 인증 시간 검증 (예: 30분 이내)
            long certifiedAt = certificationInfo.get("certified_at").asLong();
            long currentTime = System.currentTimeMillis() / 1000;

            if (currentTime - certifiedAt > 1800) { // 30분 = 1800초
                throw new IllegalArgumentException("본인인증이 만료되었습니다. 다시 인증해주세요.");
            }

            // 인증 데이터 추출
            String birthString = certificationInfo.get("birth").asText();
            LocalDate birthDate = LocalDate.parse(birthString, DateTimeFormatter.ofPattern("yyyyMMdd"));

            VerificationData result = VerificationData.builder()
                    .ci(certificationInfo.get("unique_key").asText())
                    .name(certificationInfo.get("name").asText())
                    .phone(certificationInfo.get("phone").asText())
                    .birthDate(birthDate)
                    .gender(certificationInfo.get("gender").asText())
                    .isForeigner("Y".equals(certificationInfo.get("foreigner").asText()))
                    .build();

            log.info("본인인증 데이터 추출 완료: name={}, phone={}", result.getName(), result.getPhone());
            return result;

        } catch (Exception e) {
            log.error("본인인증 검증 실패: impUid={}, 오류타입: {}, 메시지: {}", impUid, e.getClass().getSimpleName(), e.getMessage(), e);

            if (e instanceof IllegalArgumentException) {
                throw e;
            } else {
                throw new IllegalArgumentException("본인인증 검증 중 시스템 오류가 발생했습니다: " + e.getMessage());
            }
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


}
