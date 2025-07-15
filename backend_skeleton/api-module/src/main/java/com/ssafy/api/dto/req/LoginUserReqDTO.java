package com.ssafy.api.dto.req;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserReqDTO {

    @NotBlank
    private String uid;

    @NotNull
    @Pattern(regexp="^(none|sns)$") // type 필드에 들어올 수 있는 문자열을 none 또는 sns로만 제한
    @JsonProperty(defaultValue = "none")
    private String type = "none";

    @NotBlank
    private String password;


}
