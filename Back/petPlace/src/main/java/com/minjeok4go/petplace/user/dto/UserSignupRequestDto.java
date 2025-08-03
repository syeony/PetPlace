
package com.minjeok4go.petplace.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UserSignupRequestDto {
    private String userId;
    private String password;
    private String name;
    private String nickname;
    private Long rid;
    private String ci;
    private String phoneNumber;
    private String gender;
    private LocalDate birthday;
}