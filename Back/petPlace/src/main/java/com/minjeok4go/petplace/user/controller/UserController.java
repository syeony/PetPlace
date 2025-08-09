package com.minjeok4go.petplace.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.minjeok4go.petplace.user.dto.UserSignupRequestDto;
import com.minjeok4go.petplace.user.service.UserService;
import com.minjeok4go.petplace.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.minjeok4go.petplace.user.service.PortOneApiService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(
    name = "👤 User Management",
    description = """
        ## 사용자 관리 API
        
        사용자 회원가입, 중복 체크, 본인인증 등 사용자 관련 기능을 제공합니다.
        
        ### 주요 기능
        - 📝 **회원가입**: 포트원 본인인증 연동 회원가입
        - ✅ **중복 체크**: 아이디/닉네임 중복 확인
        - 🔐 **본인인증**: 포트원 API를 통한 실명 인증
        - 🧪 **테스트**: 인증 및 API 연동 테스트
        
        ### 참고사항
        - 모든 회원가입은 본인인증이 필수입니다
        - 아이디는 4-20자 영문/숫자만 가능합니다
        - 닉네임은 2-10자로 설정 가능합니다
        """
)
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PortOneApiService portOneApiService;

    @Operation(
        summary = "📋 본인인증 준비",
        description = """
            포트원 본인인증 URL을 생성합니다.
            
            ### 프로세스
            1. 이 API를 호출하여 본인인증 URL을 받습니다
            2. 받은 URL을 통해 사용자가 본인인증을 진행합니다
            3. 본인인증 완료 후 받은 `imp_uid`로 회원가입을 진행합니다
            
            ### 반환 데이터
            - `certification_url`: 본인인증 페이지 URL
            - `merchant_uid`: 고유 거래번호
            """
    )
    @ApiResponses({
        @SwaggerApiResponse(
            responseCode = "200",
            description = "본인인증 URL 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "success": true,
                          "message": "본인인증 URL 생성 성공",
                          "data": {
                            "certification_url": "https://cert.iamport.kr/certificates/abcd1234",
                            "merchant_uid": "merchant_20240101_123456"
                          }
                        }
                        """
                )
            )
        ),
        @SwaggerApiResponse(
            responseCode = "500",
            description = "본인인증 준비 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "message": "본인인증 준비 중 오류가 발생했습니다",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/certifications/prepare")
    public ResponseEntity<ApiResponse<Map<String, String>>> prepareCertification() {
        try {
            log.info("본인인증 준비 요청");
            Map<String, String> result = portOneApiService.prepareCertification();
            
            return ResponseEntity.ok(
                    ApiResponse.success("본인인증 URL 생성 성공", result)
            );
        } catch (Exception e) {
            log.error("본인인증 준비 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("본인인증 준비 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "🆕 회원가입",
        description = """
            본인인증 완료 후 일반 회원가입을 진행합니다.
            
            ### 필수 조건
            - ✅ 본인인증 완료 (`imp_uid` 필요)
            - ✅ 아이디 중복 체크 완료
            - ✅ 닉네임 중복 체크 완료
            
            ### 프로세스
            1. `/certifications/prepare`로 본인인증 URL 받기
            2. 사용자가 본인인증 완료
            3. 아이디/닉네임 중복 체크
            4. 이 API로 회원가입 요청
            
            ### 추후 추가 예정
            - 카카오 소셜 로그인 연동
            - 동네 인증 기능
            """
    )
    @ApiResponses({
        @SwaggerApiResponse(
            responseCode = "201",
            description = "회원가입 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": true,
                          "message": "회원가입이 완료되었습니다.",
                          "data": null
                        }
                        """
                )
            )
        ),
        @SwaggerApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효성 검사 실패, 중복된 정보 등)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "message": "이미 사용 중인 아이디입니다.",
                          "data": null
                        }
                        """
                )
            )
        ),
        @SwaggerApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "message": "회원가입 처리 중 오류가 발생했습니다.",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "회원가입 정보",
            required = true,
            content = @Content(
                schema = @Schema(implementation = UserSignupRequestDto.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "userName": "petlover123",
                          "password": "petplace123!",
                          "nickname": "멍멍이아빠",
                          "regionId": 1,
                          "impUid": "imp_123456789"
                        }
                        """
                )
            )
        )
        @RequestBody UserSignupRequestDto requestDto
    ) {
        try {
            log.info("회원가입 요청: userName={}", requestDto.getUserName());
            userService.signup(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("회원가입이 완료되었습니다.", null));

        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage()));

        } catch (Exception e) {
            log.error("회원가입 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("회원가입 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(
        summary = "🔍 아이디 중복 체크",
        description = """
            입력한 아이디가 이미 사용 중인지 확인합니다.
            
            ### 아이디 규칙
            - 길이: 4~20자
            - 허용 문자: 영문 대소문자, 숫자
            - 금지 문자: 특수문자, 공백, 한글
            
            ### 사용법
            회원가입 전에 반드시 중복 체크를 진행해주세요.
            """
    )
    @ApiResponses({
        @SwaggerApiResponse(
            responseCode = "200",
            description = "사용 가능한 아이디",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": true,
                          "message": "사용 가능한 아이디입니다.",
                          "data": null
                        }
                        """
                )
            )
        ),
        @SwaggerApiResponse(
            responseCode = "409",
            description = "이미 존재하는 아이디 (중복)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "message": "이미 사용 중인 아이디입니다.",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/check-username")
    public ResponseEntity<ApiResponse<Void>> checkUserNameDuplicate(
        @Parameter(
            description = "중복 체크할 아이디",
            example = "petlover123",
            required = true
        )
        @RequestParam("user_name") String userName
    ) {
        boolean isDuplicate = userService.checkUserNameDuplicate(userName).getDuplicate();

        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.failure("이미 사용 중인 아이디입니다."));
        } else {
            return ResponseEntity.ok(ApiResponse.success("사용 가능한 아이디입니다."));
        }
    }

    @Operation(
        summary = "🏷️ 닉네임 중복 체크",
        description = """
            입력한 닉네임이 이미 사용 중인지 확인합니다.
            
            ### 닉네임 규칙
            - 길이: 2~10자
            - 허용 문자: 한글, 영문, 숫자
            - 특수문자 일부 허용 (-, _, 공백)
            
            ### 사용법
            회원가입 전에 반드시 중복 체크를 진행해주세요.
            """
    )
    @ApiResponses({
        @SwaggerApiResponse(
            responseCode = "200",
            description = "사용 가능한 닉네임",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": true,
                          "message": "사용 가능한 닉네임입니다.",
                          "data": null
                        }
                        """
                )
            )
        ),
        @SwaggerApiResponse(
            responseCode = "409",
            description = "이미 존재하는 닉네임 (중복)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "message": "이미 사용 중인 닉네임입니다.",
                          "data": null
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Void>> checkNicknameDuplicate(
        @Parameter(
            description = "중복 체크할 닉네임",
            example = "멍멍이아빠",
            required = true
        )
        @RequestParam("nickname") String nickname
    ) {
        boolean isDuplicate = userService.checkNicknameDuplicate(nickname).getDuplicate();

        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.failure("이미 사용 중인 닉네임입니다."));
        } else {
            return ResponseEntity.ok(ApiResponse.success("사용 가능한 닉네임입니다."));
        }
    }

    @GetMapping("/test-auth")
    @Operation(
        summary = "🧪 토큰 인증 테스트",
        description = """
            JWT 토큰으로 인증된 사용자 정보를 확인합니다.
            
            ### 테스트 목적
            - JWT 토큰이 올바르게 전달되는지 확인
            - 인증된 사용자 정보가 올바르게 추출되는지 확인
            
            ### 사용법
            - Authorization 헤더에 `Bearer {토큰}` 추가
            - 로그인 후 받은 accessToken 사용
            """
    )
    public ResponseEntity<ApiResponse<String>> testAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return ResponseEntity.ok(
                    ApiResponse.success("토큰 인증 성공!", username)
            );
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("인증 실패"));
        }
    }

    @GetMapping("/test-portone-token")
    @Operation(
        summary = "🔧 포트원 토큰 발급 테스트",
        description = """
            포트원 API 연동 테스트를 위한 토큰 발급을 확인합니다.
            
            ### 테스트 내용
            - 포트원 API 키 설정 확인
            - 토큰 발급 API 연동 확인
            - 네트워크 연결 상태 확인
            """
    )
    public ResponseEntity<ApiResponse<String>> testPortOneToken() {
        try {
            log.info("포트원 토큰 발급 테스트 시작");

            String accessToken = portOneApiService.getAccessToken();

            return ResponseEntity.ok(ApiResponse.success(
                    "포트원 토큰 발급 성공! Token: " + accessToken.substring(0, Math.min(30, accessToken.length())) + "..."
            ));
        } catch (Exception e) {
            log.error("포트원 토큰 발급 테스트 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("포트원 토큰 발급 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/test-portone-cert/{impUid}")
    @Operation(
        summary = "🔍 포트원 인증 정보 조회 테스트",
        description = """
            특정 본인인증 거래의 정보를 조회합니다.
            
            ### 테스트 목적
            - 본인인증 완료 후 정보 조회 확인
            - 포트원 API 연동 상태 확인
            - 인증 정보 파싱 테스트
            
            ### 사용법
            - 본인인증 완료 후 받은 `imp_uid` 사용
            - 테스트용으로만 사용하세요
            """
    )
    public ResponseEntity<ApiResponse<String>> testPortOneCert(
        @Parameter(
            description = "포트원 본인인증 거래번호",
            example = "imp_123456789",
            required = true
        )
        @PathVariable String impUid
    ) {
        try {
            log.info("포트원 인증 정보 조회 테스트 시작: impUid={}", impUid);

            JsonNode result = portOneApiService.getCertificationInfo(impUid);

            if (result == null) {
                return ResponseEntity.ok(ApiResponse.failure("결과가 null입니다"));
            }

            return ResponseEntity.ok(ApiResponse.success(
                    "포트원 인증 정보 조회 성공! 응답: " + result.toPrettyString()
            ));
        } catch (Exception e) {
            log.error("포트원 인증 정보 조회 테스트 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("포트원 인증 정보 조회 실패: " + e.getMessage()));
        }
    }
}
