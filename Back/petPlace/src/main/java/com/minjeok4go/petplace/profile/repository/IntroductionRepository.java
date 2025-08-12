package com.minjeok4go.petplace.profile.repository;

import com.minjeok4go.petplace.profile.entity.Introduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntroductionRepository extends JpaRepository<Introduction, Long> {

    Optional<Introduction> findByUserId(Long user_id);
}
