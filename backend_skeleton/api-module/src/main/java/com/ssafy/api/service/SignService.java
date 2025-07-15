//package com.ssafy.api.service;
//
//import com.ssafy.core.code.JoinCode;
//import com.ssafy.core.code.YNCode;
//import com.ssafy.core.entity.User;
//import com.ssafy.core.exception.ApiMessageException;
//import com.ssafy.core.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class SignService {
//    private final UserRepository userRepository;
//
//
//    /**
//     * id로 회원정보 조회
//     * @param id
//     * @return
//     * @throws Exception
//     */
//    public User findUserById(long id) throws Exception{
//        User user = userRepository.findById(id).orElseThrow( () -> new ApiMessageException("존재하지 않는 회원정보입니다.") );
//        return user;
//    }
//
//    /**
//     * uid로 user 조회
//     * @param uid
//     * @return
//     * @throws Exception
//     */
//    public User findByUid(String uid, YNCode isBind) throws Exception{
//        return userRepository.findByUid(uid, isBind);
//    }
//
//
//    /**
//     * 회원가입 후 userId 리턴
//     * @param user
//     * @return
//     */
//    @Transactional(readOnly = false)
//    public long userSignUp(User user){
//        User signUpUser = userRepository.save(user);
//        return signUpUser.getId();
//    }
//
//}
//

package com.ssafy.api.service;

import com.ssafy.core.code.JoinCode;
import com.ssafy.core.code.YNCode;
import com.ssafy.core.entity.User;
import com.ssafy.core.exception.ApiMessageException;
import com.ssafy.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
// 기본은 읽기 전용으로 두고, 쓰기 메서드에만 @Transactional를 다시 붙입니다.
@Transactional(readOnly = true)
public class SignService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 1) ID로 회원정보 조회
     * @param id 회원의 PK
     * @return User 엔티티
     * @throws ApiMessageException 없으면 예외 던짐
     */
    public User findUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiMessageException("존재하지 않는 회원정보입니다."));
    }

    /**
     * 2) UID + isBind(Y/N)로 회원 조회
     * @param uid 로그인 아이디 또는 SNS UID
     * @param isBind 계정 활성화 여부 (YNCode)
     * @return User 엔티티
     * @throws ApiMessageException 없으면 예외
     */
    public User findByUid(String uid, YNCode isBind) {
        // 레포에서 User를 가져와서(없으면 null)
        User user = userRepository.findByUid(uid, isBind);

        //  없으면 예외 던지기
        if (user == null) {
            throw new ApiMessageException("등록된 사용자가 아닙니다.");
        }

        return user;
    }
    // 2-1) 가입된 사용자(일반 or SNS) 존재 여부 체크

    public boolean userExists(String uid, JoinCode joinType) {
        return userRepository.existsByUidAndJoinType(uid, joinType);
    }

    /**
     * 3) 회원가입 처리
     * @param user 가입할 User 엔티티 (uid, password, name, email, phone, address 등 세팅된 상태)
     * @return 저장된 회원의 PK(ID)
     * @throws ApiMessageException 이미 가입된 회원이면 예외
     */
    @Transactional(readOnly = false)
    public long userSignUp(User user) {
        // 3-1) 중복 가입 방지: 같은 uid + joinType이 이미 있으면 예외
        if (userRepository.existsByUidAndJoinType(user.getUid(), user.getJoinType())) {
            throw new ApiMessageException("이미 가입된 회원입니다.");
        }

        // 3-2) 비밀번호 암호화 (평문 password → 해시값)
        user.updatePassword(passwordEncoder.encode(user.getPassword()));

        // 3-3) 계정 사용 가능 여부 기본값 지정 (Y: 활성화)
//        user.updateIsBind(YNCode.Y);

        // 3-4) 실제 저장 & 생성된 ID를 반환
        User saved = userRepository.save(user);
        return saved.getId();
    }

    /**
     * 4) 로그인 처리
     * @param uid 로그인 아이디 또는 SNS UID
     * @param joinType 회원 가입 타입 (JoinCode.none 또는 JoinCode.sns)
     * @param rawPassword 로그인 시 입력된 비밀번호
     * @return 인증된 User 엔티티
     * @throws ApiMessageException 사용자 없음 또는 비밀번호 불일치 시 예외
     */
    public User userLogin(String uid,
                          JoinCode joinType,
                          String rawPassword) {
        // 4-1) 가입된 회원인지 조회
        User user = userRepository.findByUidAndJoinType(uid, joinType)
                .orElseThrow(() -> new ApiMessageException("등록된 사용자가 아닙니다."));

        // 4-2) 일반회원(none)인 경우에만 비밀번호 검증
        if (joinType == JoinCode.none) {
            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                throw new ApiMessageException("비밀번호가 일치하지 않습니다.");
            }
        }

        // 4-3) SNS 회원인 경우(rawPassword) 검증을 건너뛰고 바로 통과
        return user;
    }
}
















































