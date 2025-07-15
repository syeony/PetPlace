package kr.co.skeleton.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class SignInResponse {
    private Integer id;
    private String token;
    private String nickname;
    private int output;
    private String msg;
}
