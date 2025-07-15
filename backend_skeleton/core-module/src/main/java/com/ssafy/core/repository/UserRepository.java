package com.ssafy.core.repository;

import com.ssafy.core.entity.User;
import com.ssafy.core.code.JoinCode;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepoCommon{
    // 일반로그인/SNS로그인 구분해서 조회
    Optional<User> findByUidAndJoinType(String uid, JoinCode joinType);

    // 소셜가입 존재 여부 체크
    boolean existsByUidAndJoinType(String uid, JoinCode joinType);
}
