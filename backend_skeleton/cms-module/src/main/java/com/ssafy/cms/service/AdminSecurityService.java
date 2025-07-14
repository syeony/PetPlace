package com.ssafy.cms.service;

import com.ssafy.cms.repo.AdminSecurityRepository;
import com.ssafy.core.entity.AdminSecurity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Spring Security 활용을 위한 Service
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AdminSecurityService implements UserDetailsService {

    @Autowired
    private final AdminSecurityRepository adminSecurityRepository;

    /**
     * Spring Security 필수 메서드 구현
     * @param username (user_id)
     * @return AdminSecurity
     * @throws UsernameNotFoundException 유저가 없을 때 예외 발생
     */
    @Override
    public AdminSecurity loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AdminSecurity> adminSecurityWrapper = adminSecurityRepository.findByAdminId(username);

        if(!adminSecurityWrapper.isPresent()){
            throw new UsernameNotFoundException("존재하지 않는 사용자");
        }
        AdminSecurity adminSecurity = adminSecurityWrapper.get();

        return new AdminSecurity(adminSecurity.getAdminId(), adminSecurity.getPassword(), adminSecurity.getAuthId(), adminSecurity.getAdminSeq()
        , adminSecurity.getName(), adminSecurity.getEmail(), adminSecurity.getTelno(), adminSecurity.getPhone(), adminSecurity.getRegister(), adminSecurity.getModifier());
    }
}
