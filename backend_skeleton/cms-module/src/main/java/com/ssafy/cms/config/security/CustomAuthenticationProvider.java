package com.ssafy.cms.config.security;

import com.ssafy.core.entity.AdminSecurity;
import com.ssafy.cms.service.AdminSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final AdminSecurityService adminSecurityService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        // AuthenticationFilter에서 생성된 토큰으로부터 아이디와 비밀번호를 조회한다.
        String adminId = token.getName();
        String password = (String) token.getCredentials();

        AdminSecurity adminSecurity = (AdminSecurity) adminSecurityService.loadUserByUsername(adminId);


        if(!passwordEncoder.matches(password, adminSecurity.getPassword())){
            throw new BadCredentialsException(adminSecurity.getAdminId() + "Invalid password");
        }

        System.out.println("adminSecurity.getAuthorities() :: " + adminSecurity.getAuthorities());

        return new UsernamePasswordAuthenticationToken(adminSecurity, password, adminSecurity.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
