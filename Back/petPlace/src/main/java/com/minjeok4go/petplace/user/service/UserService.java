// src/main/java/com/minjeok4go/petplace/user/service/UserService.java
package com.minjeok4go.petplace.user.service;

import com.minjeok4go.petplace.user.domain.User;
import com.minjeok4go.petplace.user.dto.CheckDuplicateResponseDto;
import com.minjeok4go.petplace.user.dto.UserSignupRequestDto;
import com.minjeok4go.petplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public void signup(UserSignupRequestDto requestDto) {
        // 1. 아이디 중복 확인
        if (userRepository.existsByUserId(requestDto.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        
        // 1-2. 닉네임 중복 확인
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        
        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 3. 사용자 정보로 User 엔티티 생성
        User user = User.builder()
                .userId(requestDto.getUserId())
                .password(encodedPassword) // 암호화된 비밀번호 저장
                .name(requestDto.getName())
                .nickname(requestDto.getNickname())
                .rid(requestDto.getRid())
                .ci(requestDto.getCi())
                .phoneNumber(requestDto.getPhoneNumber())
                .gender(requestDto.getGender())
                .birthday(requestDto.getBirthday())
                .build();

        // 4. 데이터베이스에 저장
        userRepository.save(user);
    }

    // 아이디 중복 체크
    @Transactional(readOnly = true)
    public CheckDuplicateResponseDto checkUserIdDuplicate(String userId) {
        boolean isDuplicate = userRepository.existsByUserId(userId);
        String message = isDuplicate ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.";
        return new CheckDuplicateResponseDto(isDuplicate, message);
    }

    // 닉네임 중복 체크
    @Transactional(readOnly = true)
    public CheckDuplicateResponseDto checkNicknameDuplicate(String nickname) {
        boolean isDuplicate = userRepository.existsByNickname(nickname);
        String message = isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.";
        return new CheckDuplicateResponseDto(isDuplicate, message);
    }

    // AuthService에서 호출할 사용자 조회 메서드
    @Transactional(readOnly = true)
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디입니다."));
    }


}
