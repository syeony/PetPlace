package com.minjeok4go.petplace.user.controller;

import com.minjeok4go.petplace.user.dto.UserSignupRequestDto;
import com.minjeok4go.petplace.user.service.UserService;
import com.minjeok4go.petplace.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Tag(name = "User API", description = "사용자 관련 API 명세서입니다.")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @Operation(summary = "회원가입", description = "본인인증 완료 후 회원가입을 진행합니다. 추후 카카오 연동 추가 예정 , 동네 인증도")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody UserSignupRequestDto requestDto) {
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

    // 아이디 중복 체크
    @Operation(summary = "아이디 중복 체크", description = "입력한 아이디가 이미 사용 중인지 확인합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용 가능한 아이디")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 아이디 (중복)")
    @PostMapping("/check-username")
    public ResponseEntity<ApiResponse<Void>> checkUserNameDuplicate(@RequestParam("user_name") String userName) {
        boolean isDuplicate = userService.checkUserNameDuplicate(userName).getDuplicate();

        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.failure("이미 사용 중인 아이디입니다."));
        } else {
            return ResponseEntity.ok(ApiResponse.success("사용 가능한 아이디입니다."));
        }
    }

    // 닉네임 중복 체크
    @Operation(summary = "닉네임 중복 체크", description = "입력한 닉네임이 이미 사용 중인지 확인합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용 가능한 닉네임")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 닉네임 (중복)")
    @PostMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Void>> checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
        boolean isDuplicate = userService.checkNicknameDuplicate(nickname).getDuplicate();

        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.failure("이미 사용 중인 닉네임입니다."));
        } else {
            return ResponseEntity.ok(ApiResponse.success("사용 가능한 닉네임입니다."));
        }
    }

    @GetMapping("/test-auth")
    @Operation(summary = "토큰 인증 테스트", description = "JWT 토큰으로 인증된 사용자 정보를 확인합니다.")
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
}