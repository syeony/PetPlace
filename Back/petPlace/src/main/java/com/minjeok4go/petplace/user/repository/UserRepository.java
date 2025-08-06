package com.minjeok4go.petplace.user.repository;

import com.minjeok4go.petplace.user.entity.LoginType;
import com.minjeok4go.petplace.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

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

    // id 기반 조회 메서드 추가
    Optional<User> findById(Long id);
    boolean existsById(Long id);

    // --- 소셜 로그인용 메서드 (수정됨) ---

    /**
     * 소셜 ID로 사용자 조회 (로그인 타입 무관)
     */
    @Query("SELECT u FROM User u WHERE u.socialId = :socialId")
    Optional<User> findBySocialId(@Param("socialId") String socialId);

    /**
     * 소셜 ID 존재 여부 확인 (로그인 타입 무관)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.socialId = :socialId")
    boolean existsBySocialId(@Param("socialId") String socialId);

    /**
     * 특정 로그인 타입의 소셜 ID로 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.loginType = :loginType AND u.socialId = :socialId")
    Optional<User> findByLoginTypeAndSocialId(@Param("loginType") LoginType loginType, @Param("socialId") String socialId);

    /**
     * 특정 로그인 타입의 소셜 ID 존재 여부 확인
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.loginType = :loginType AND u.socialId = :socialId")
    boolean existsByLoginTypeAndSocialId(@Param("loginType") LoginType loginType, @Param("socialId") String socialId);

    /**
     * CI로 모든 사용자 조회 (연동 가능성을 고려한 메서드)
     */
    @Query("SELECT u FROM User u WHERE u.ci = :ci")
    List<User> findAllByCi(@Param("ci") String ci);
}
