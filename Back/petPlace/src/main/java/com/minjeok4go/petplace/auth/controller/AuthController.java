package com.minjeok4go.petplace.auth.controller;

import com.minjeok4go.petplace.auth.dto.*;
import com.minjeok4go.petplace.auth.jwt.JwtTokenProvider;
import com.minjeok4go.petplace.auth.service.AuthService;
import com.minjeok4go.petplace.auth.service.RefreshTokenService;
import com.minjeok4go.petplace.auth.service.SocialAuthService;
import com.minjeok4go.petplace.common.dto.ApiResponse;
import com.minjeok4go.petplace.user.dto.UserLoginRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Slf4j
@Tag(
        name = "🔐 Auth Management",
        description = "## 인증 및 인가 API"
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final SocialAuthService socialAuthService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(
            summary = "일반 로그인",
            description = "사용자의 아이디와 비밀번호로 로그인하고, Access/Refresh 토큰과 사용자 정보를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "로그인 실패 (아이디 또는 비밀번호 불일치)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"아이디 또는 비밀번호가 일치하지 않습니다.\", \"data\": null}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"로그인 처리 중 오류가 발생했습니다.\", \"data\": null}"))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "로그인에 필요한 아이디와 비밀번호",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserLoginRequestDto.class))
            )
            @RequestBody UserLoginRequestDto requestDto) {
        try {
            TokenDto tokenDto = authService.login(requestDto);
            return ResponseEntity.ok(tokenDto);
        } catch (IllegalArgumentException e) {
            log.warn("로그인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("로그인 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("로그인 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(
            summary = "Access Token 갱신",
            description = "유효한 Refresh Token을 사용하여 만료된 Access Token을 새로 발급받습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenRefreshResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "유효하지 않은 Refresh Token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenRefreshResponseDto.class),
                            examples = @ExampleObject(value = "{\"accessToken\": null, \"refreshToken\": null, \"message\": \"Refresh Token이 유효하지 않습니다.\", \"success\": false}"))
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDto> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "토큰 갱신에 필요한 Refresh Token",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TokenRefreshRequestDto.class))
            )
            @RequestBody TokenRefreshRequestDto request) {
        try {
            TokenRefreshResponseDto response = refreshTokenService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(TokenRefreshResponseDto.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TokenRefreshResponseDto.failure("토큰 갱신 중 오류가 발생했습니다."));
        }
    }

    @Operation(
            summary = "로그아웃",
            description = "서버에 저장된 사용자의 Refresh Token을 삭제하여 로그아웃을 처리합니다. 클라이언트에서도 Access Token을 삭제해야 합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "로그아웃 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"로그아웃 성공\", \"data\": null}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"인증되지 않은 사용자\", \"data\": null}"))
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                Long userId = Long.parseLong(authentication.getName());
                refreshTokenService.deleteByUserId(userId);
                return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.failure("인증되지 않은 사용자"));
            }
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("로그아웃 처리 중 오류가 발생했습니다."));
        }
    }

    // ========== 소셜 로그인 API ==========

    @Operation(
            summary = "소셜 로그인",
            description = """
            소셜 플랫폼(카카오 등)에서 발급받은 Access Token으로 로그인을 시도합니다.
            
            ### 처리 결과
            - **기존 사용자**: 로그인 성공 후 토큰 발급 (`status: EXISTING_USER`)
            - **신규 사용자**: 본인인증 및 회원가입을 위한 임시 토큰 발급 (`status: NEW_USER`)
            - **연동 가능 사용자**: 기존 계정과 연동 가능한 신규 소셜 로그인 시 임시 토큰 발급 (`status: LINKABLE_USER`)
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "소셜 로그인 처리 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SocialLoginResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SocialLoginResponse.class),
                            examples = @ExampleObject(value = "{\"status\": \"ERROR\", \"message\": \"소셜 로그인 처리 중 오류가 발생했습니다.\", \"tokenDto\": null, \"tempToken\": null, \"linkableUserId\": null}"))
            )
    })
    @PostMapping("/social/login")
    public ResponseEntity<SocialLoginResponse> socialLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "소셜 로그인 요청 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SocialLoginRequest.class))
            )
            @RequestBody SocialLoginRequest request) {
        try {
            SocialLoginResponse response = socialAuthService.processSocialLogin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("소셜 로그인 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SocialLoginResponse.error("소셜 로그인 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(
            summary = "소셜 회원가입",
            description = """
            소셜 로그인 시 신규 사용자로 판별된 경우, 본인인증 완료 후 이 API를 통해 회원가입을 완료합니다.
            
            ### 필수 조건
            - 소셜 로그인 API 호출 후 받은 `tempToken`
            - 포트원 본인인증 완료 후 받은 `imp_uid`
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "소셜 회원가입 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (닉네임 중복 등)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"이미 사용 중인 닉네임입니다.\", \"data\": null}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "유효하지 않은 임시 토큰",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"유효하지 않은 임시 토큰입니다.\", \"data\": null}"))
            )
    })
    @PostMapping("/social/signup")
    public ResponseEntity<?> socialSignup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "소셜 회원가입 요청 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SocialSignupRequest.class))
            )
            @RequestBody SocialSignupRequest request) {
        try {
            if (request.getTempToken() == null || request.getTempToken().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.failure("임시 토큰이 필요합니다."));
            }
            try {
                Map<String, Object> tempTokenClaims = jwtTokenProvider.getTempTokenClaims(request.getTempToken());
                String tokenProvider = (String) tempTokenClaims.get("provider");
                if (!tokenProvider.equals(request.getProvider().name())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.failure("임시 토큰의 소셜 플랫폼 정보가 일치하지 않습니다."));
                }
            } catch (Exception e) {
                log.warn("유효하지 않은 임시 토큰으로 회원가입 시도: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.failure("유효하지 않은 임시 토큰입니다."));
            }
            TokenDto tokenDto = socialAuthService.processSocialSignup(request);
            log.info("소셜 회원가입 성공 - provider: {}", request.getProvider());
            return ResponseEntity.ok(tokenDto);

        } catch (IllegalArgumentException e) {
            log.warn("소셜 회원가입 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("소셜 회원가입 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("회원가입 처리 중 오류가 발생했습니다."));
        }
    }

    // ========== 토큰 검증 API ==========

    @Operation(
            summary = "Access Token 유효성 검증",
            description = "현재 요청의 Authorization 헤더에 담긴 Access Token이 유효한지 확인합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "토큰 검증 완료 (유효/만료/비정상)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenValidationResponse.class))
            )
    })
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken() {
        try {
            String token = getCurrentToken();
            if (token == null) {
                return ResponseEntity.ok(TokenValidationResponse.invalid("헤더에 토큰이 존재하지 않습니다."));
            }
            TokenValidationResponse response = authService.validateAccessToken(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("토큰 검증 중 오류 발생: ", e);
            return ResponseEntity.ok(TokenValidationResponse.invalid("토큰 검증 중 서버 오류가 발생했습니다."));
        }
    }

    private String getCurrentToken() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        } catch (Exception e) {
            log.warn("요청 컨텍스트에서 토큰 추출 실패: ", e);
        }
        return null;
    }
}

