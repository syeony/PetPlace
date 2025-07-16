package kr.co.skeleton.model;

public class SignUpRequest {
    public String uid;
    public String password;
    public String name;
    public String email;
    public String phone;
    public String address;
    public String addressDetail;
    public String type = "none";  // 기본값: 일반회원가입

    public SignUpRequest() {}
}
