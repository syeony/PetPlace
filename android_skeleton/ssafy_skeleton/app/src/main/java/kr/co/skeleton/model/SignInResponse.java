package kr.co.skeleton.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class SignInResponse {
    @SerializedName("id")
    private Integer id;
    private String token;
}
