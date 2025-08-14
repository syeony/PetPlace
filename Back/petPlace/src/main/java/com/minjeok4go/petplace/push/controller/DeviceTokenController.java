package com.minjeok4go.petplace.push.controller;

import com.minjeok4go.petplace.push.dto.CreateTokenRequest;
import com.minjeok4go.petplace.push.dto.TokenResponse;
import com.minjeok4go.petplace.push.dto.UpdateTokenRequest;
import com.minjeok4go.petplace.push.service.DeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Token API", description = "토큰 API")
@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @Operation(
            summary = "디바이스 토큰 생성",
            description = "토큰으로 받아온 유저 ID의 디바이스 토큰을 저장합니다."
    )
    @PostMapping
    public TokenResponse register(@Valid @RequestBody CreateTokenRequest req,
                                  @AuthenticationPrincipal String tokenUserId) {
        return deviceTokenService.register(tokenUserId, req);
    }

    @Operation(
            summary = "디바이스 토큰 비활성화",
            description = "토큰으로 받아온 유저 ID의 디바이스 토큰을 비활성화합니다."
    )
    @DeleteMapping
    public ResponseEntity<Void> unregister(@RequestParam String token,
                                           @AuthenticationPrincipal String tokenUserId) {
        deviceTokenService.unregister(tokenUserId, token);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "디바이스 토큰 목록 반환",
            description = "토큰으로 받아온 유저 ID의 디바이스 토큰들을 반환합니다."
    )
    @GetMapping
    public List<TokenResponse> list(@AuthenticationPrincipal String tokenUserId) {
        return deviceTokenService.list(tokenUserId);
    }

    @Operation(
            summary = "디바이스 토큰 전체 비활성화",
            description = "토큰으로 받아온 유저 ID의 디바이스 토큰들을 비활성화합니다."
    )
    @DeleteMapping("/all")
    public ResponseEntity<Void> deactivateAll(@AuthenticationPrincipal String tokenUserId) {
        deviceTokenService.deactivateAll(tokenUserId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "디바이스 토큰 갱신",
            description = "토큰으로 받아온 유저 ID의 디바이스 토큰을 갱신합니다."
    )
    @PostMapping("/rotate")
    public TokenResponse rotate(@Valid @RequestBody UpdateTokenRequest req,
                                @AuthenticationPrincipal String tokenUserId) {
        return deviceTokenService.replaceToken(tokenUserId, req);
    }
}