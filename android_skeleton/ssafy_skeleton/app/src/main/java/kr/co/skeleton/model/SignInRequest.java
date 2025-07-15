package kr.co.skeleton.model;

import lombok.Data;

@Data
public class SignInRequest {
    private String uid;
    private String password;
    public SignInRequest(String uid, String password){
        this.uid = uid;
        this.password = password;
    }
}
