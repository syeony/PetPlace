package kr.co.skeleton.network;

import io.reactivex.Flowable;

import kr.co.skeleton.model.CommonRes;
import kr.co.skeleton.model.SignInResponse;
import okhttp3.MultipartBody;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitService {

    //로그인
    @GET("api/sign/login")
    Flowable<CommonRes<SignInResponse>> login(
            @Query("deviceUid") String deviceUid,
            @Query("password") String password,
            @Query("token") String token,
            @Query("type") String type,
            @Query("uid") String uid
    );
}
