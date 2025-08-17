package com.minjeok4go.petplace.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.minjeok4go.petplace.common.dto.ApiResponse;
import com.minjeok4go.petplace.user.dto.CheckDuplicateResponseDto;
import com.minjeok4go.petplace.user.dto.DongAuthenticationResponse;
import com.minjeok4go.petplace.user.dto.UserSignupRequestDto;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.service.PortOneApiService;
import com.minjeok4go.petplace.user.service.RegionData;
import com.minjeok4go.petplace.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(
        name = "👤 User Management",
        description = "사용자 관리 API"
)
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PortOneApiService portOneApiService;



    @Operation(
            summary = "🆕 회원가입",
            description = """
            본인인증 완료 후 일반 회원가입을 진행합니다.
            
            ### 필수 조건
            - ✅ 본인인증 완료 (`imp_uid` 필요)
            - ✅ 아이디 중복 체크 완료
            - ✅ 닉네임 중복 체크 완료
            
            ### 프로세스
            1. (클라이언트) 사용자가 본인인증을 완료하고 `imp_uid`를 획득합니다.
            2. (클라이언트) 아이디/닉네임 중복 체크를 완료합니다.
            3. 이 API로 회원가입을 요청합니다.
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "회원가입 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"회원가입이 완료되었습니다.\", \"data\": null}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (유효성 검사 실패, 중복된 정보 등)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"이미 사용 중인 아이디입니다.\", \"data\": null}"))
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입에 필요한 사용자 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserSignupRequestDto.class))
            )
            @RequestBody UserSignupRequestDto requestDto) {
        try {
            userService.signup(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("회원가입이 완료되었습니다.", null));
        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("회원가입 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(
            summary = "🔍 아이디 중복 체크",
            description = "입력한 아이디가 이미 사용 중인지 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "사용 가능한 아이디",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"사용 가능한 아이디입니다.\", \"data\": null}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "이미 존재하는 아이디 (중복)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"이미 사용 중인 아이디입니다.\", \"data\": null}"))
            )
    })
    @PostMapping("/check-username")
    public ResponseEntity<ApiResponse<Void>> checkUserNameDuplicate(
            @Parameter(description = "중복 체크할 아이디", example = "petlover123", required = true)
            @RequestParam("user_name") String userName) {
        CheckDuplicateResponseDto result = userService.checkUserNameDuplicate(userName);
        if (result.getDuplicate()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(result.getMessage()));
        } else {
            return ResponseEntity.ok(ApiResponse.success(result.getMessage()));
        }
    }

    @Operation(
            summary = "🏷️ 닉네임 중복 체크",
            description = "입력한 닉네임이 이미 사용 중인지 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "사용 가능한 닉네임",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"사용 가능한 닉네임입니다.\", \"data\": null}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "이미 존재하는 닉네임 (중복)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"이미 사용 중인 닉네임입니다.\", \"data\": null}"))
            )
    })
    @PostMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Void>> checkNicknameDuplicate(
            @Parameter(description = "중복 체크할 닉네임", example = "멍멍이아빠", required = true)
            @RequestParam("nickname") String nickname) {
        CheckDuplicateResponseDto result = userService.checkNicknameDuplicate(nickname);
        if (result.getDuplicate()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(result.getMessage()));
        } else {
            return ResponseEntity.ok(ApiResponse.success(result.getMessage()));
        }
    }

    @GetMapping("/test-auth")
    @Operation(summary = "🧪 토큰 인증 테스트 (개발용)")
    public ResponseEntity<ApiResponse<String>> testAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success("토큰 인증 성공!", username));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("인증 실패"));
        }
    }

    @GetMapping("/test-portone-token")
    @Operation(summary = "🔧 포트원 토큰 발급 테스트 (개발용)")
    public ResponseEntity<ApiResponse<String>> testPortOneToken() {
        try {
            String accessToken = portOneApiService.getAccessToken();
            return ResponseEntity.ok(ApiResponse.success("포트원 토큰 발급 성공! Token: " + accessToken.substring(0, Math.min(30, accessToken.length())) + "..."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.failure("포트원 토큰 발급 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/certifications/prepare")
    @Operation(
            summary = "📄 본인인증 준비",
            description = """
            포트원을 통한 본인인증 URL을 생성합니다.
            
            ### 프로세스
            1. 클라이언트가 이 API를 호출하여 본인인증 URL을 획득합니다.
            2. 클라이언트는 반환된 URL로 사용자를 리다이렉트합니다.
            3. 사용자가 본인인증을 완료하면 `imp_uid`를 획득합니다.
            4. 획득한 `imp_uid`로 회원가입을 진행합니다.
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "본인인증 URL 생성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"본인인증 URL이 생성되었습니다.\", \"data\": {\"certification_url\": \"https://cert.iamport.kr/...\", \"merchant_uid\": \"cert_1234567890\"}}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"본인인증 URL 생성 중 오류가 발생했습니다.\", \"data\": null}"))
            )
    })
    public ResponseEntity<ApiResponse<Object>> prepareCertification() {
        try {
            var result = portOneApiService.prepareCertification();
            return ResponseEntity.ok(ApiResponse.success("본인인증 URL이 생성되었습니다.", result));
        } catch (Exception e) {
            log.error("본인인증 URL 생성 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("본인인증 URL 생성 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/test-portone-cert/{impUid}")
    @Operation(summary = "🔍 포트원 인증 정보 조회 테스트 (개발용)")
    public ResponseEntity<ApiResponse<String>> testPortOneCert(@PathVariable String impUid) {
        try {
            JsonNode result = portOneApiService.getCertificationInfo(impUid);
            if (result == null) {
                return ResponseEntity.ok(ApiResponse.failure("결과가 null입니다"));
            }
            return ResponseEntity.ok(ApiResponse.success("포트원 인증 정보 조회 성공! 응답: " + result.toPrettyString()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.failure("포트원 인증 정보 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 동네 인증 API
     * 현재 로그인한 사용자의 위치를 기반으로 동네를 인증합니다.
     */
    @Operation(
            summary = "🏠 동네 인증",
            description = """
        사용자의 현재 위치 좌표를 받아 해당하는 행정동을 판별하고 사용자 정보를 업데이트합니다.
        
        ### 사용법
        1. GPS를 통해 사용자의 현재 위치 (위도, 경도)를 획득합니다.
        2. 이 API를 호출하여 동네 인증을 진행합니다.
        3. 성공 시 사용자의 지역 정보가 업데이트됩니다.
        
        ### 좌표계
        - **WGS84** 좌표계를 사용합니다.
        - 위도(lat): 33.0 ~ 43.0 (대한민국 영역)
        - 경도(lon): 124.0 ~ 132.0 (대한민국 영역)
        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "동네 인증 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                        {
                          "success": true,
                          "message": "동네 인증이 완료되었습니다.",
                          "data": {
                            "regionId": 4719000000,
                            "regionName": "진미동"
                          }
                        }
                        """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (좌표 범위 초과, 지역 찾을 수 없음 등)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                        {
                          "success": false,
                          "message": "대한민국 영역 내의 좌표가 아닙니다.",
                          "data": null
                        }
                        """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                        {
                          "success": false,
                          "message": "로그인이 필요합니다.",
                          "data": null
                        }
                        """))
            )
    })
    @PostMapping("/me/dong-authentication")
    public ResponseEntity<ApiResponse<DongAuthenticationResponse>> authenticateDong(
            @Parameter(description = "위도 (WGS84)", example = "37.5665", required = true)
            @RequestParam("lat") Double lat,
            @Parameter(description = "경도 (WGS84)", example = "126.9780", required = true)
            @RequestParam("lon") Double lon) {

        // 파라미터 유효성 검증
        if (lat == null || lon == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("위도(lat)와 경도(lon) 파라미터가 필요합니다."));
        }

        try {
            // 🔥 수정: 인증되지 않은 사용자도 접근 가능하도록 변경
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // 인증된 사용자인 경우 - 사용자 정보 업데이트
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                Long userId = Long.parseLong(authentication.getName());
                log.info("동네 인증 요청 (인증된 사용자) - 사용자: {}, 좌표: ({}, {})", userId, lat, lon);
                
                DongAuthenticationResponse response = userService.authenticateDong(userId, lat, lon);
                return ResponseEntity.ok(ApiResponse.success("동네 인증이 완료되었습니다.", response));
            } 
            // 인증되지 않은 사용자인 경우 - 지역 정보만 반환 (DB 업데이트 없음)
            else {
                log.info("동네 인증 요청 (비인증 사용자) - 좌표: ({}, {})", lat, lon);
                
                DongAuthenticationResponse response = userService.findRegionByCoordinates(lat, lon);
                return ResponseEntity.ok(ApiResponse.success("지역 조회가 완료되었습니다. (로그인 후 동네 인증을 완료하세요)", response));
            }

        } catch (IllegalArgumentException e) {
            log.warn("동네 인증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));

        } catch (Exception e) {
            log.error("동네 인증 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("동네 인증 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 테스트용 API - 좌표로 지역 확인 (로그인 불필요, DB 업데이트 없음)
     */
    @Operation(
            summary = "🧪 좌표 지역 확인 (테스트용)",
            description = "로그인 없이 좌표만으로 해당 지역을 확인할 수 있는 테스트용 API입니다. 사용자 정보는 업데이트되지 않습니다."
    )
    @GetMapping("/test/region-by-coordinates")
    public ResponseEntity<ApiResponse<DongAuthenticationResponse>> testRegionByCoordinates(
            @Parameter(description = "위도 (WGS84)", example = "37.5665", required = true)
            @RequestParam("lat") Double lat,
            @Parameter(description = "경도 (WGS84)", example = "126.9780", required = true)
            @RequestParam("lon") Double lon) {

        if (lat == null || lon == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("위도(lat)와 경도(lon) 파라미터가 필요합니다."));
        }

        try {
            DongAuthenticationResponse response = userService.findRegionByCoordinates(lat, lon);
            return ResponseEntity.ok(ApiResponse.success("지역 조회 성공", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));

        } catch (Exception e) {
            log.error("지역 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("지역 조회 중 오류가 발생했습니다."));
        }
    }
}
