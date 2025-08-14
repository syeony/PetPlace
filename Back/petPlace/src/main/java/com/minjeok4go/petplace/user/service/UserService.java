package com.minjeok4go.petplace.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.minjeok4go.petplace.region.repository.RegionRepository;
import com.minjeok4go.petplace.user.dto.DongAuthenticationResponse;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.dto.CheckDuplicateResponseDto;
import com.minjeok4go.petplace.user.dto.UserSignupRequestDto;
import com.minjeok4go.petplace.user.dto.VerificationData;
import com.minjeok4go.petplace.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.minjeok4go.petplace.region.entity.Region;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PortOneApiService portOneApiService;
    private final RegionRepository regionRepository;
    private final LocationService locationService;


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
     * 아이디 중복 체크
     */
    @Transactional(readOnly = true)
    public CheckDuplicateResponseDto checkUserNameDuplicate(String userName) {
        boolean isDuplicate = userRepository.existsByUserName(userName);
        String message = isDuplicate ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.";
        return new CheckDuplicateResponseDto(isDuplicate, message);
    }

    /**
     * 닉네임 중복 체크
     */
    @Transactional(readOnly = true)
    public CheckDuplicateResponseDto checkNicknameDuplicate(String nickname) {
        boolean isDuplicate = userRepository.existsByNickname(nickname);
        String message = isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.";
        return new CheckDuplicateResponseDto(isDuplicate, message);
    }

    /**
     * 사용자명으로 사용자 조회 (AuthService에서 로그인 시 사용)
     */
    @Transactional(readOnly = true)
    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디입니다."));
    }

    /**
     * ID로 사용자 조회 (AuthService 및 다른 서비스에서 사용)
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
    }

    // === 아래 private 메서드들은 기존과 동일하게 유지 ===

    private VerificationData verifyAndGetUserData(String impUid) {
        try {
            log.info("본인인증 검증 시작: impUid={}", impUid);

            JsonNode responseData = portOneApiService.getCertificationInfo(impUid);
            log.info("포트원 API 응답 데이터: {}", responseData.toPrettyString());

            if (responseData.isMissingNode() || responseData.isNull()) {
                throw new IllegalArgumentException("포트원으로부터 유효한 응답 데이터를 받지 못했습니다.");
            }

            boolean isCertified = responseData.path("certified").asBoolean(false);
            if (!isCertified) {
                throw new IllegalArgumentException("본인인증이 완료되지 않았습니다.");
            }

            String ci = responseData.path("unique_key").asText(null);
            if (ci == null || ci.trim().isEmpty()) {
                log.warn("CI(unique_key)가 비어있습니다. 전화번호로 임시 CI 생성");
                String phone = responseData.path("phone").asText();
                ci = "TEMP_CI_" + phone + "_" + System.currentTimeMillis();
            }

            long certifiedAt = responseData.path("certified_at").asLong();
            long currentTime = System.currentTimeMillis() / 1000;
            if (currentTime - certifiedAt > 1800) { // 30분 = 1800초
                throw new IllegalArgumentException("본인인증 유효 시간이 초과되었습니다. 다시 시도해주세요.");
            }

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

    private void validateSignupRequest(UserSignupRequestDto requestDto, VerificationData verificationData) {
        if (userRepository.existsByUserName(requestDto.getUserName())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        if (userRepository.existsByCi(verificationData.getCi())) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        }
        if (userRepository.existsByPhoneNumber(verificationData.getPhone())) {
            throw new IllegalArgumentException("이미 가입된 전화번호입니다.");
        }
    }

    private User createUser(UserSignupRequestDto requestDto, VerificationData verificationData) {
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        // ✅ 1. DTO에서 regionId를 가져옵니다 (없으면 기본값 1L 사용).
        Long regionId = requestDto.getRegionId() != null ? requestDto.getRegionId() : 1L;

        // ✅ 2. regionId를 사용해 DB에서 실제 Region 엔티티를 조회합니다.
        // 만약 해당 ID의 Region이 DB에 없으면 예외가 발생하여 회원가입이 중단됩니다.
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역 ID입니다: " + regionId));


        return User.builder()
                .userName(requestDto.getUserName())
                .password(encodedPassword)
                .name(verificationData.getName())
                .nickname(requestDto.getNickname())
                .region(region)
                .ci(verificationData.getCi())
                .phoneNumber(verificationData.getPhone())
                .gender(verificationData.getGender())
                .birthday(verificationData.getBirthDate())
                .build();
    }

    public User updateImage(User user) {
        return userRepository.save(user);
    }

    public User updateDefaultPet(User user) {
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * 동네 인증 기능
     * 사용자의 현재 위치를 기반으로 행정동을 판별하고 사용자 정보를 업데이트합니다.
     */
    @Transactional
    public DongAuthenticationResponse authenticateDong(Long userId, double lat, double lon) {
        log.info("동네 인증 시작 - 사용자 ID: {}, 좌표: ({}, {})", userId, lat, lon);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 좌표 유효성 검증
        if (lat < 33.0 || lat > 43.0 || lon < 124.0 || lon > 132.0) {
            throw new IllegalArgumentException("대한민국 영역 내의 좌표가 아닙니다.");
        }

        // 3. LocationService를 통해 지역 찾기
        RegionData regionData = locationService.findRegionFromWgs84(lon, lat);
        if (regionData == null) {
            throw new IllegalArgumentException("해당 좌표에 해당하는 지역을 찾을 수 없습니다.");
        }

        // 4. Region 엔티티 조회 또는 생성
        Region region = regionRepository.findById(regionData.getId())
                .orElseGet(() -> {
                    log.info("새로운 지역 생성 - ID: {}, 이름: {}", regionData.getId(), regionData.getName());
                    // geometry 컬럼을 사용자의 현재 위치로 설정
                    Point centerPoint = locationService.getPointFromWgs84(lon, lat);
                    Region newRegion = Region.builder()
                            .id(regionData.getId())
                            .name(regionData.getName())
                            .geometry(centerPoint)
                            .build();
                    return regionRepository.save(newRegion);
                });

        // 5. 사용자의 지역 정보 업데이트
        user.updateRegion(region);
        userRepository.save(user);

        log.info("사용자 {}의 동네 인증 완료: {} ({})", userId, region.getName(), region.getId());

        return DongAuthenticationResponse.of(region.getId(), region.getName());
    }

    /**
     * 테스트용 메서드 - 좌표만으로 지역 확인 (DB 업데이트 없음)
     */
    public DongAuthenticationResponse findRegionByCoordinates(double lat, double lon) {
        if (lat < 33.0 || lat > 43.0 || lon < 124.0 || lon > 132.0) {
            throw new IllegalArgumentException("대한민국 영역 내의 좌표가 아닙니다.");
        }

        RegionData regionData = locationService.findRegionFromWgs84(lon, lat);
        if (regionData == null) {
            throw new IllegalArgumentException("해당 좌표에 해당하는 지역을 찾을 수 없습니다.");
        }

        return DongAuthenticationResponse.of(regionData.getId(), regionData.getName());
    }

}