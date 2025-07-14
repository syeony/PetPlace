package com.ssafy.core.entity;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Spring Security 활용을 위한 Entity
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Data
@Table(name = "admin")
public class AdminSecurity extends BaseEntity implements UserDetails {

    @Id
    @Column(name = "admin_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminSeq;

    @Column(name = "admin_id")
    private String adminId;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "telno")
    private String telno;

    @Column(name = "phone")
    private String phone;

    @Column(name = "register")
    @Nullable
    private Integer register;

    @Column(name = "modifier")
    @Nullable
    private Integer modifier;

    @Column(name = "auth_id")
    private String authId;

    @Builder
    public AdminSecurity(String adminId, String password, String authId, Long adminSeq, String name, String email, String telno, String phone
    , Integer register, Integer modifier){
        this.adminId = adminId;
        this.password = password;
        this.authId = authId;
        this.adminSeq = adminSeq;
        this.name = name;
        this.email = email;
        this.telno = telno;
        this.phone = phone;
        this.register = register;
        this.modifier = modifier;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> roles = new HashSet<>();
        for (String role : authId.split(",")) {
            roles.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return roles;
    }

    @Override
    public String getUsername() {
        return adminId;
    }

    // 계정 만료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        // 만료되었는지 확인하는 로직
        // true : 만료되지 않음
        return false;
    }

    // 계정 잠금 여부 반환
    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금되었는지 확인하는 로직
        // true : 잠금되지 않음
        return false;
    }

    // 패스워드의 만료 여부 반환
    @Override
    public boolean isCredentialsNonExpired() {
        // 패스워드가 만료되었는지 확인하는 로직
        // true : 만료되지 않음
        return false;
    }

    // 계정 사용 가능 여부 반환
    @Override
    public boolean isEnabled() {
        // 계정이 사용 가능한지 확인하는 로직
        // true : 사용 가능
        return false;
    }
}
