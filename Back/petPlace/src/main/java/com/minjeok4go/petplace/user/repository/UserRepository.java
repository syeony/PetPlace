package com.minjeok4go.petplace.user.repository;

import com.minjeok4go.petplace.user.entity.LoginType; // ✅ LoginType 임포트
import com.minjeok4go.petplace.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // --- 기존 메서드 (변경 없음) ---
    @Query("SELECT u FROM User u WHERE u.userName = :userName")
    Optional<User> findByUserName(@Param("userName") String userName);

    Optional<User> findByNickname(String nickname);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userName = :userName")
    boolean existsByUserName(@Param("userName") String userName);

    boolean existsByNickname(String nickname);
    boolean existsByCi(String ci);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByCi(String ci);

    // --- 소셜 로그인용 메서드 추가 ---

    // [추가] 로그인 타입과 소셜 ID로 사용자를 찾는 메서드
    @Query("SELECT u FROM User u WHERE u.loginType = :loginType AND u.socialId = :socialId")
    Optional<User> findBySocialId(@Param("loginType") LoginType loginType, @Param("socialId") String socialId);

    // [추가] 로그인 타입과 소셜 ID로 사용자 존재 여부를 확인하는 메서드
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.loginType = :loginType AND u.socialId = :socialId")
    boolean existsBySocialId(@Param("loginType") LoginType loginType, @Param("socialId") String socialId);
}