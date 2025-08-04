package com.minjeok4go.petplace.user.repository;

import com.minjeok4go.petplace.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // userName으로 사용자를 찾는 메소드
    //SELECT * FROM User WHERE user_name = ? 와 같은 쿼리문을 만드는 것
    @Query("SELECT u FROM User u WHERE u.userName = :userName")
    Optional<User> findByUserName(@Param("userName") String userName);

    // nickname으로 사용자를 찾는 메소드 (중복 체크용)
    //SELECT * FROM User WHERE nickname = ? 와 같은 쿼리문을 만드는 것
    Optional<User> findByNickname(String nickname);

    // userName 존재 여부 확인 (중복 체크용)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userName = :userName")
    boolean existsByUserName(@Param("userName") String userName);

    // nickname 존재 여부 확인 (중복 체크용)
    boolean existsByNickname(String nickname);

}