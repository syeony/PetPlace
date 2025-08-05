package com.minjeok4go.petplace.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationData {
    private String ci;
    private String name;
    private String phone;
    private LocalDate birthDate;
    private String gender;
    private Boolean isForeigner;
}