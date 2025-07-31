// src/main/java/com/minjeok4go/petplace/user/repository/UserRepository.java
package com.minjeok4go.petplace.user.repository;

import com.minjeok4go.petplace.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // userId로 사용자를 찾는 메소드
    //SELECT * FROM User WHERE user_id = ? 와 같은 쿼리문을 만드는 것
    Optional<User> findByUserId(String userId);

    // nickname으로 사용자를 찾는 메소드 (중복 체크용)
    //SELECT * FROM User WHERE nickname = ? 와 같은 쿼리문을 만드는 것
    Optional<User> findByNickname(String nickname);

    // userId 존재 여부 확인 (중복 체크용)
    boolean existsByUserId(String userId);

    // nickname 존재 여부 확인 (중복 체크용)
    boolean existsByNickname(String nickname);

}