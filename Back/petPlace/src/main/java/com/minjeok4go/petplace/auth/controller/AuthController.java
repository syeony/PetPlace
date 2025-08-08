package com.minjeok4go.petplace.auth.controller;

import com.minjeok4go.petplace.auth.dto.*;
import com.minjeok4go.petplace.auth.jwt.JwtTokenProvider;
import com.minjeok4go.petplace.auth.service.AuthService;
import com.minjeok4go.petplace.auth.service.RefreshTokenService;
import com.minjeok4go.petplace.auth.service.SocialAuthService;
import com.minjeok4go.petplace.common.dto.ApiResponse;
import com.minjeok4go.petplace.user.dto.UserLoginRequestDto;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Auth API", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final SocialAuthService socialAuthService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인 합니다. 추가적인 유저의 정보도 반환합니다.")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDto requestDto) {
        try {
            TokenDto tokenDto = authService.login(requestDto);
            return ResponseEntity.ok(tokenDto);  // TokenDto 직접 반환 (기존 방식 유지)
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

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    public ResponseEntity<TokenRefreshResponseDto> refreshToken(@RequestBody TokenRefreshRequestDto request) {
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

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자의 Refresh Token을 삭제하여 로그아웃 처리합니다.")
    public ResponseEntity<ApiResponse<Void>> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
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

    @PostMapping("/social/login")
    @Operation(summary = "소셜 로그인", description = "카카오, 네이버, 구글 등 소셜 플랫폼으로 로그인합니다.")
    public ResponseEntity<SocialLoginResponse> socialLogin(@RequestBody SocialLoginRequest request) {
        try {
            SocialLoginResponse response = socialAuthService.processSocialLogin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("소셜 로그인 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SocialLoginResponse.error("소셜 로그인 처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/social/signup")
    @Operation(summary = "소셜 회원가입", description = "본인인증 완료 후 소셜 계정으로 회원가입합니다.")
    public ResponseEntity<?> socialSignup(@RequestBody SocialSignupRequest request) {
        log.info("Signup 요청으로 들어온 tempToken: {}", request.getTempToken());

        try {
            // 1. 임시 토큰 검증
            if (request.getTempToken() == null || request.getTempToken().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.failure("임시 토큰이 필요합니다."));
            }

            // 2. 임시 토큰 유효성 검증 및 정보 추출
            try {
                Map<String, Object> tempTokenClaims = jwtTokenProvider.getTempTokenClaims(request.getTempToken());
                String tokenProvider = (String) tempTokenClaims.get("provider");

                // 3. 요청의 provider와 토큰 provider 일치 여부 확인
                if (!tokenProvider.equals(request.getProvider().name())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.failure("임시 토큰의 소셜 플랫폼 정보가 일치하지 않습니다."));
                }

                log.info("소셜 회원가입 요청 검증 완료 - provider: {}, tempToken 유효", request.getProvider());
                
            } catch (Exception e) {
                log.warn("유효하지 않은 임시 토큰으로 회원가입 시도: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.failure("유효하지 않은 임시 토큰입니다."));
            }

            // 4. 회원가입 처리
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

    @PostMapping("/social/check-linkable")
    @Operation(summary = "계정 연동 확인", description = "CI 기반으로 연동 가능한 기존 계정이 있는지 확인합니다.")
    public ResponseEntity<SocialLoginResponse> checkLinkableAccount(
            @RequestParam String ci,
            @RequestParam String socialId,
            @RequestParam String provider) {
        try {
            SocialLoginResponse response = socialAuthService.checkLinkableAccount(ci, socialId, provider);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("계정 연동 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SocialLoginResponse.error("계정 확인 중 오류가 발생했습니다."));
        }
    }

    // ========== 토큰 검증 API ==========

    @GetMapping("/validate")
    @Operation(summary = "Access Token 유효성 검증", 
               description = "현재 사용자의 Access Token이 유효한지 확인합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 검증 완료")
    public ResponseEntity<TokenValidationResponse> validateToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(TokenValidationResponse.invalid("인증되지 않은 요청입니다"));
            }
            
            // 현재 요청의 토큰 추출
            String token = getCurrentToken();
            if (token == null) {
                return ResponseEntity.ok(TokenValidationResponse.invalid("토큰을 찾을 수 없습니다"));
            }
            
            TokenValidationResponse response = authService.validateAccessToken(token);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("토큰 검증 중 오류 발생: ", e);
            return ResponseEntity.ok(TokenValidationResponse.invalid("토큰 검증 중 오류가 발생했습니다"));
        }
    }

    @PostMapping("/validate-token")
    @Operation(summary = "특정 토큰 유효성 검증", 
               description = "제공된 Access Token의 유효성을 확인합니다.")
    public ResponseEntity<TokenValidationResponse> validateSpecificToken(@RequestBody TokenValidationRequest request) {
        try {
            TokenValidationResponse response = authService.validateAccessToken(request.getToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("특정 토큰 검증 중 오류 발생: ", e);
            return ResponseEntity.ok(TokenValidationResponse.invalid("토큰 검증 중 오류가 발생했습니다"));
        }
    }

    // 현재 요청의 토큰을 추출하는 헬퍼 메서드
    private String getCurrentToken() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        } catch (Exception e) {
            log.warn("토큰 추출 실패: ", e);
        }
        return null;
    }
}
