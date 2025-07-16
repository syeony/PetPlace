package kr.co.skeleton.network;

import io.reactivex.Flowable;

import kr.co.skeleton.model.CommonRes;
import kr.co.skeleton.model.EmailRequest;
import kr.co.skeleton.model.SignInRequest;
import kr.co.skeleton.model.SignInResponse;
import kr.co.skeleton.model.SignUpRequest;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitService {

    @POST("/api/sign/login")
    Flowable<SignInResponse> login(@Body SignInRequest request);

    @POST("api/sign/regProfile")
    Flowable<CommonRes<Void>> registerProfile(@Body SignUpRequest request);

    @POST("api/auth/send/email")
    Flowable<CommonRes<Void>> sendEmail(@Body EmailRequest request);

    @GET("api/auth/check/email")
    Flowable<CommonRes<Void>> checkEmail(
            @Query("email") String email,
            @Query("authNum") String authNum
    );

    @GET("api/sign/exists/social")
    Flowable<CommonRes<Boolean>> checkSocialExists(@Query("uid") String uid);
}

