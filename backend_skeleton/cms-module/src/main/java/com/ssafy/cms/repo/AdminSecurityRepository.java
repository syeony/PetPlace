package com.ssafy.cms.repo;

import com.ssafy.core.entity.AdminSecurity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Security 활용을 위한 Repository
 */
public interface AdminSecurityRepository extends JpaRepository<AdminSecurity, Long> {
        Optional<AdminSecurity> findByAdminId(String username);
}
