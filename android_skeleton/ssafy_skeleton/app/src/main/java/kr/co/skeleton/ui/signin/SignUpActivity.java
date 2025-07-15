package kr.co.skeleton.ui.signin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.jakewharton.rxbinding3.view.RxView;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kr.co.skeleton.R;
import kr.co.skeleton.model.EmailRequest;
import kr.co.skeleton.model.SignUpRequest;
import kr.co.skeleton.network.RetrofitManager;

public class SignUpActivity extends AppCompatActivity {

    private Context mContext;
    private boolean isEmailVerified = false;  // ✅ 이메일 인증 상태 플래그

    private AppCompatEditText edit_id, edit_pw, edit_pw_check, edit_name,
            edit_email_id, edit_verify_code, edit_phone,
            edit_address, edit_address_detail;

    private AutoCompleteTextView spinner_email_domain;

    private Button btn_send_code, btn_verify, btn_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mContext = this;

        findViewById(R.id.btn_back).setOnClickListener(v -> finish()); // 뒤로가기

        initLayout();
        initListener();
    }

    private void initLayout() {
        edit_id = findViewById(R.id.edit_id);
        edit_pw = findViewById(R.id.edit_pw);
        edit_pw_check = findViewById(R.id.edit_pw_check);
        edit_name = findViewById(R.id.edit_name);
        edit_email_id = findViewById(R.id.edit_email_id);
        spinner_email_domain = findViewById(R.id.spinner_email_domain);
        edit_verify_code = findViewById(R.id.edit_verify_code);
        edit_phone = findViewById(R.id.edit_phone);
        edit_address = findViewById(R.id.edit_address);
        edit_address_detail = findViewById(R.id.edit_address_detail);

        btn_send_code = findViewById(R.id.btn_send_code);
        btn_verify = findViewById(R.id.btn_verify);
        btn_submit = findViewById(R.id.btn_submit);
    }

    @SuppressLint("CheckResult")
    private void initListener() {
        // 🔹 이메일 인증코드 발송
        RxView.clicks(btn_send_code)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(unit -> {
                    Log.d("EMAIL_BTN", "버튼 눌림");

                    String email = edit_email_id.getText().toString() + "@" + spinner_email_domain.getText().toString();

                    RetrofitManager.getService().sendEmail(new EmailRequest(email))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(res -> {
                                Log.d("EMAIL_SUCCESS", "이메일 전송 성공: " + res.toString());
                                Toast.makeText(mContext, "이메일 발송 성공", Toast.LENGTH_SHORT).show();
                            }, err -> {
                                Log.e("EMAIL_ERROR", "이메일 전송 실패", err);
                                Toast.makeText(mContext, "이메일 발송 실패: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });

        // 🔹 인증 확인
        RxView.clicks(btn_verify)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(unit -> {
                    String email = edit_email_id.getText().toString() + "@" + spinner_email_domain.getText().toString();
                    String code = edit_verify_code.getText().toString();

                    RetrofitManager.getService().checkEmail(email, code)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(res -> {
                                isEmailVerified = true; // ✅ 인증 완료
                                Toast.makeText(mContext, "인증 성공", Toast.LENGTH_SHORT).show();
                            }, err -> {
                                isEmailVerified = false;
                                Toast.makeText(mContext, "인증 실패", Toast.LENGTH_SHORT).show();
                            });
                });

        // 🔹 회원가입
        RxView.clicks(btn_submit)
                .throttleFirst(2, TimeUnit.SECONDS)
                .subscribe(unit -> {
                    if (!isEmailVerified) {
                        Toast.makeText(mContext, "이메일 인증을 완료해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = edit_id.getText().toString().trim();
                    String pw = edit_pw.getText().toString().trim();
                    String pwCheck = edit_pw_check.getText().toString().trim();
                    String name = edit_name.getText().toString().trim();
                    String emailId = edit_email_id.getText().toString().trim();
                    String emailDomain = spinner_email_domain.getText().toString().trim();
                    String phone = edit_phone.getText().toString().trim();
                    String addr = edit_address.getText().toString().trim();
                    String addrDetail = edit_address_detail.getText().toString().trim();

                    if (uid.isEmpty() || pw.isEmpty() || pwCheck.isEmpty() || name.isEmpty() ||
                            emailId.isEmpty() || emailDomain.isEmpty() || phone.isEmpty() ||
                            addr.isEmpty() || addrDetail.isEmpty()) {
                        Toast.makeText(mContext, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!pw.equals(pwCheck)) {
                        Toast.makeText(mContext, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SignUpRequest req = new SignUpRequest();
                    req.uid = uid;
                    req.password = pw;
                    req.name = name;
                    req.email = emailId + "@" + emailDomain;
                    req.phone = phone;
                    req.address = addr;
                    req.addressDetail = addrDetail;

                    RetrofitManager.getService().registerProfile(req)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(res -> {
                                Toast.makeText(mContext, "회원가입 성공", Toast.LENGTH_SHORT).show();
                                finish();
                            }, err -> {
                                Toast.makeText(mContext, "회원가입 실패", Toast.LENGTH_SHORT).show();
                            });
                });
    }
}
