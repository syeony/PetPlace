
package com.minjeok4go.petplace.auth.service;

import com.minjeok4go.petplace.auth.dto.TokenDto;
import com.minjeok4go.petplace.auth.dto.TokenValidationResponse;
import com.minjeok4go.petplace.auth.jwt.JwtTokenProvider;
import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.user.dto.UserLoginRequestDto;
import com.minjeok4go.petplace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public TokenDto login(UserLoginRequestDto requestDto) {
        // 1. 아이디로 사용자 조회 (UserService 위임)
        User user = userService.findByUserName(requestDto.getUserName());

        // 2. 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. Access Token과 Refresh Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 4. Refresh Token 저장 (RefreshToken의 userId는 user_name을 참조)
        refreshTokenService.saveOrUpdate(user.getId(), refreshToken);

        // 5. 토큰 및 사용자 정보 반환
        return TokenDto.of(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUserName(),
                user.getNickname(),
                user.getUserImgSrc(),
                user.getLevel(),
                user.getDefaultPetId(),
                user.getRegionId()
        );
    }

    /**
     * Access Token 유효성 검증
     */
    public TokenValidationResponse validateAccessToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return TokenValidationResponse.invalid("토큰이 제공되지 않았습니다");
        }
        
        JwtTokenProvider.TokenValidationResult result = jwtTokenProvider.validateTokenWithDetails(token);
        
        if (result.isValid()) {
            // 실제 사용자가 존재하는지 확인 (선택사항)
            try {
                User user = userService.getUserById(result.getUserId());
                return TokenValidationResponse.valid(user.getId(), result.getExpiresIn());
            } catch (Exception e) {
                return TokenValidationResponse.invalid("토큰의 사용자 정보를 찾을 수 없습니다");
            }
        } else {
            return TokenValidationResponse.invalid(result.getMessage());
        }
    }
}
