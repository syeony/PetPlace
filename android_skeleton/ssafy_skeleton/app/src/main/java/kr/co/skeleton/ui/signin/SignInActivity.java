package kr.co.skeleton.ui.signin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jakewharton.rxbinding3.view.RxView;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kr.co.skeleton.R;
import kr.co.skeleton.common.Constant;
import kr.co.skeleton.common.PrefManager;
import kr.co.skeleton.model.SignInRequest;
import kr.co.skeleton.model.SignInResponse;
import kr.co.skeleton.network.RetrofitManager;

public class SignInActivity extends AppCompatActivity {
        private Context mContext;
        private AppCompatEditText edit_id; //아이디
        private AppCompatEditText edit_passwd; //패스워드
        private AppCompatImageView img_autologin; //자동로그인
        private AppCompatTextView txt_login; //로그인버튼

        private AppCompatTextView txt_join;
        private static Integer CHECK = 0; //자동 로그인 체크여부
        private long back= 0; // 뒤로가기시 사용
        private String fcmToken = ""; //fcm토큰

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_sign_in);

            mContext = this;
            init();
        }

        private void init() {
            initLayout();
            initListener();
        }

        private void initLayout() {
            edit_id = findViewById(R.id.edit_id);
            edit_passwd = findViewById(R.id.edit_passwd);
            img_autologin = findViewById(R.id.img_autologin);
            txt_login = findViewById(R.id.txt_login);
            txt_join = findViewById(R.id.txt_join);
        }

        @SuppressLint("CheckResult")
        private void initListener() {
            //자동로그인 체크여부
            img_autologin.setOnClickListener(view -> {
                if(CHECK == 1){
                    img_autologin.setImageDrawable(getDrawable(R.drawable.ico_check_off));
                    CHECK = 0;
                    PrefManager.setAutoLogin(mContext,false);
                }else{
                    img_autologin.setImageDrawable(getDrawable(R.drawable.ico_check_on));
                    CHECK = 1;
                    PrefManager.setAutoLogin(mContext,true);
                }
            });

            //로그인
            RxView.clicks(txt_login)
                    .throttleFirst(Constant.CLICK_DURATION, TimeUnit.SECONDS) //중복 터치를 방지하기 위해 사용
                    .subscribe(unit -> login());
            RxView.clicks(txt_join)
                    .throttleFirst(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(unit -> {
                        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                        startActivity(intent);
                    });




        }

    private static final String TAG = "SignInActivity";
        //일반 로그인
        @SuppressLint("CheckResult")
        private void login() {
            Log.d(TAG, "login: 로그인 진입");
            String deviceUUID = UUID.randomUUID().toString();
            String id = edit_id.getText().toString();
            String passwd = edit_passwd.getText().toString();

<<<<<<< HEAD
            // 로그: 로그인 요청 파라미터 확인
            Log.d("LOGIN", "로그인 요청 시작");
            Log.d("LOGIN", "입력한 ID: " + id);
            Log.d("LOGIN", "입력한 PW: " + passwd);
            Log.d("LOGIN", "디바이스 UUID: " + deviceUUID);
            Log.d("LOGIN", "FCM Token: " + fcmToken);

            SignInRequest request = new SignInRequest();
            request.setUid(id);
            request.setPassword(passwd);
            request.setToken(fcmToken);
            request.setType("none");
            request.setDeviceUid(deviceUUID);

            RetrofitManager.getService().login(request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        Constant.AUTH_TOKEN = result.getToken();
                        PrefManager.setUUID(mContext, deviceUUID);
                        PrefManager.setFbToken(mContext, fcmToken);
                        PrefManager.setToken(mContext, result.getToken());
                        PrefManager.setUserKey(mContext, result.getId());
                        Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show();
<<<<<<< HEAD
                    }, throwable -> {
                        Log.e("로그인실패", "로그인 중 예외 발생", throwable);
                        Toast.makeText(this, "서버 오류: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
=======
            RetrofitManager.getService().login(new SignInRequest(id, passwd))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
//                        if (result.getOutput() == Constant.RESPONSE_OK) {
                            Constant.AUTH_TOKEN = result.getToken();
                            PrefManager.setUUID(mContext,deviceUUID);
                            PrefManager.setFbToken(mContext,fcmToken);
                            PrefManager.setToken(mContext,result.getToken());
                            PrefManager.setUserKey(mContext,result.getId());
                            Toast.makeText(this,"로그인 성공", Toast.LENGTH_SHORT).show();
                           // startTargetActivity(MainActivity.class);
//                        } else {
//                            Toast.makeText(this, getString(R.string.invalid_login), Toast.LENGTH_SHORT).show();
//                        }
                    }, throwable -> {
                        throwable.getMessage();
                        Log.d(TAG, "login: "+throwable.getMessage());
                        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
>>>>>>> origin/sjh
=======
                    }, throwable -> {
                        Log.e("로그인실패", "로그인 중 예외 발생", throwable);
                        Toast.makeText(this, "서버 오류: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
>>>>>>> origin/osy
                    });

        }



}