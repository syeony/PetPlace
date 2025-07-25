package kr.co.skeleton.model;

import lombok.Data;


@Data
public class SignInRequest {
    private String uid;
    private String password;
    private String token;
    private String type;
    private String deviceUid;

    public SignInRequest() {}  // ← 이거 추가

    // getter/setter 생략
=======
    public SignInRequest(String uid, String password){
        this.uid = uid;
        this.password = password;
    }
>>>>>>> origin/sjh
=======
    public SignInRequest() {}
>>>>>>> origin/ldh
}
