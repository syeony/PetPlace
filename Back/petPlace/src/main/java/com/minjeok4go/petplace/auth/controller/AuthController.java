package com.minjeok4go.petplace.auth.controller;

import com.minjeok4go.petplace.auth.domain.RefreshToken;
import com.minjeok4go.petplace.auth.dto.TokenRefreshRequestDto;
import com.minjeok4go.petplace.auth.dto.TokenRefreshResponseDto;
import com.minjeok4go.petplace.auth.jwt.JwtTokenProvider;
import com.minjeok4go.petplace.auth.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@Tag(name = "Auth API", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급합니다.")
    @ApiResponse(responseCode = "200", description = "토큰 갱신 성공")
    @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    public ResponseEntity<TokenRefreshResponseDto> refreshToken(@RequestBody TokenRefreshRequestDto request) {
        try {
            String refreshToken = request.getRefreshToken();

            // 1. Refresh Token 유효성 검증
            if (!refreshTokenService.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(TokenRefreshResponseDto.failure("유효하지 않은 Refresh Token입니다."));
            }

            // 2. Refresh Token으로 사용자 조회
            Optional<RefreshToken> tokenOptional = refreshTokenService.findByRefreshToken(refreshToken);
            if (tokenOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(TokenRefreshResponseDto.failure("Refresh Token을 찾을 수 없습니다."));
            }

            String userId = tokenOptional.get().getUserId();

            // 3. 새로운 Access Token과 Refresh Token 생성
            String newAccessToken = jwtTokenProvider.createAccessToken(userId);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

            // 4. 새로운 Refresh Token 저장
            refreshTokenService.saveOrUpdate(userId, newRefreshToken);

            // 5. 기존 Refresh Token 삭제
            // refreshTokenService.deleteByRefreshToken(refreshToken);

            TokenRefreshResponseDto response = TokenRefreshResponseDto.success(newAccessToken, newRefreshToken);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TokenRefreshResponseDto.failure("토큰 갱신 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자의 Refresh Token을 삭제하여 로그아웃 처리합니다.")
    public ResponseEntity<String> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                String userId = authentication.getName();

                // 사용자의 모든 Refresh Token 삭제
                refreshTokenService.deleteByUserId(userId);

                return ResponseEntity.ok("로그아웃 성공");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자");
            }
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그아웃 처리 중 오류가 발생했습니다.");
        }
    }
}
