package com.ssafy.cms.config.security;

import com.ssafy.cms.service.AdminSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    // 인증 프로세스
    // 1. http Request -> Authenticationfilter
    // 2. Authenticationfilter -> UsernamePasswordAuthenticationToken
    // 3. AuthenticationFilter -> AuthenticationManager<interface>
    // 4. AuthenticationManager<interface> -> AuthenticationProvider
    // 5. AuthenticationProvider -> AdminSecutiryService(Spring Security의 UserDetailsService를 상속받음)
    // 6. AdminSecutiryService -> AdminSecurity(Spring Security의 UserDetails를 상속받음)
    // 7. AdminSecurity -> AdminSecutiryService
    // 8. AdminSecutiryService -> AuthenticationProvider
    // 9. AuthenticationProvider -> AuthenticationManager<interface>
    // 10. AuthenticationManager<interface> -> AuthenticationFilter
    // 11. AuthenticationFilter -> SecurityContextHolder[SecurityContext[Authentication]]

    // 1. 사용자가 아이디 비밀번호로 로그인을 요청함
    // 2. AuthenticationFilter에서 UsernamePasswordAuthenticationToken을 생성하여 AuthenticaionManager에게 전달
    // 3. AuthenticaionManager는 등록된 AuthenticaionProvider(들)을 조회하여 인증을 요구함
    // 4. AuthenticaionProvider는 UserDetailsService를 통해 입력받은 아이디에 대한 사용자 정보를 DB에서 조회함
    // 5. 입력받은 비밀번호를 암호화하여 DB의 비밀번호화 매칭되는 경우 인증이 성공된 UsernameAuthenticationToken을 생성하여 AuthenticaionManager로 반환함
    // 6. AuthenticaionManager는 UsernameAuthenticaionToken을 AuthenticaionFilter로 전달함
    // 7. AuthenticationFilter는 전달받은 UsernameAuthenticationToken을 LoginSuccessHandler로 전송하고, SecurityContextHolder에 저장함


    @Autowired
    private final AdminSecurityService adminSecurityService;

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
        web.ignoring().antMatchers("/css/**", "/js/**", "/img/**", "/lib/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
//                        .antMatchers("/cms/**").hasRole("ADMIN")
//                        .antMatchers("/partner/**").hasRole("PARTNER")
                        .antMatchers("/common/**").permitAll()
                        .antMatchers("/common/inc/**").permitAll()
                        .antMatchers("/loginform.do").permitAll()
                        .antMatchers("/cms/menu/**").permitAll()
//                        .antMatchers("/cms/menu/**").hasRole("ADMIN")
                .and()
                        .formLogin()
                        .loginPage("/loginform.do")
                        .usernameParameter("user_id")
                        .passwordParameter("user_pwd")
                        .defaultSuccessUrl("/main.do")
                        .permitAll()
                .and()
                        .logout()
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout.do"))
                        .logoutSuccessUrl("/loginform.do")
                        .invalidateHttpSession(true)
                .and().addFilterBefore(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        ;

    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter() throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManager());
        customAuthenticationFilter.setFilterProcessesUrl("/login.do");
        customAuthenticationFilter.setAuthenticationSuccessHandler(customLoginSuccessHandler());
        customAuthenticationFilter.afterPropertiesSet();
        return customAuthenticationFilter;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomLoginSuccessHandler customLoginSuccessHandler() {
        return new CustomLoginSuccessHandler();
    }

    @Bean
    public CustomAuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider(adminSecurityService, bCryptPasswordEncoder());
    }

    @Override public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) {
        authenticationManagerBuilder.authenticationProvider(customAuthenticationProvider());
    }


}
