package com.minjeok4go.petplace.auth.controller;

import com.minjeok4go.petplace.auth.dto.TokenDto;
import com.minjeok4go.petplace.auth.dto.TokenRefreshRequestDto;
import com.minjeok4go.petplace.auth.dto.TokenRefreshResponseDto;
import com.minjeok4go.petplace.auth.service.AuthService;
import com.minjeok4go.petplace.auth.service.RefreshTokenService;
import com.minjeok4go.petplace.common.dto.ApiResponse;
import com.minjeok4go.petplace.user.dto.UserLoginRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Auth API", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

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
                String userName = authentication.getName();
                refreshTokenService.deleteByUserName(userName);  // RefreshToken의 userId는 userName을 저장
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
}
