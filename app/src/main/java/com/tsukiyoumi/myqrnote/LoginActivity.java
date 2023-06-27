package com.tsukiyoumi.myqrnote;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dou361.dialogui.DialogUIUtils;
import com.google.gson.Gson;

import java.util.HashMap;

import com.tsukiyoumi.myqrnote.entity.UserEntity;
import com.tsukiyoumi.myqrnote.http.Api;
import com.tsukiyoumi.myqrnote.http.ApiListener;
import com.tsukiyoumi.myqrnote.http.ApiUtil;
import com.tsukiyoumi.myqrnote.http.UniteApi;
import com.tsukiyoumi.myqrnote.util.StatusBarUtil;


public class LoginActivity extends AppCompatActivity {

    private EditText loginAccount;
    private EditText loginPassword;
    private Boolean accountFlag = false;
    private Boolean passwordFlag = false;
    private Button loginGoBtn;

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StatusBarUtil.setStatusBarMode(this, true, R.color.colorBack);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        init();
        initListener();
    }

    private void init() {
        loginAccount = findViewById(R.id.loginAccount);
        loginPassword = findViewById(R.id.loginPassword);
        loginGoBtn = findViewById(R.id.loginGoBtn);
        loginGoBtn.setEnabled(false);
    }

    private void initListener() {

        loginGoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = DialogUIUtils.showLoading(LoginActivity.this,
                        "验证中...", false, false,
                        false, true)
                        .show();
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("userId", loginAccount.getText().toString());
                hashMap.put("userPassword", loginPassword.getText().toString());
                new UniteApi(ApiUtil.LOGIN, hashMap).post(new ApiListener() {
                    @Override
                    public void success(Api api) {
                        dialog.dismiss();
                        UniteApi uniteApi = (UniteApi) api;
                        Gson gson = new Gson();
                        UserEntity user = gson.fromJson(uniteApi.getJsonData().toString(), UserEntity.class);
                        if (user.getUserId() != 0) {
                            // 保存在设置中
                            ApiUtil.USER_ID = String.valueOf(user.getUserId());
                            ApiUtil.USER_AVATAR = user.getUserAvatar();
                            ApiUtil.USER_NAME = user.getUserName();
                            // 保存在系统文件中
                            SharedPreferences.Editor editor = getSharedPreferences("accountState", MODE_PRIVATE).edit();
                            editor.putString("userId", String.valueOf(user.getUserId()));
                            editor.putString("userAvatar", user.getUserAvatar());
                            editor.putString("userName", user.getUserName());
                            editor.putBoolean("isPermit", true);
                            editor.commit();
                            // 登录成功进行跳转
                            setResult(Activity.RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(getBaseContext(), "账号或密码错误", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(Api api) {
                        dialog.dismiss();
                        Toast.makeText(getBaseContext(), "服务器错误", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        loginAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                accountFlag = s.length() > 0;
                if (accountFlag && passwordFlag) {
                    loginGoBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_btn_theme_circle));
                    loginGoBtn.setTextColor(getResources().getColor(R.color.colorLight));
                    loginGoBtn.setEnabled(true);
                } else {
                    loginGoBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_btn_theme_pale_circle));
                    loginGoBtn.setTextColor(getResources().getColor(R.color.colorDark));
                    loginGoBtn.setEnabled(false);
                }
            }
        });

        loginPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                passwordFlag = s.length() > 0;
                if (accountFlag && passwordFlag) {
                    loginGoBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_btn_theme_circle));
                    loginGoBtn.setTextColor(getResources().getColor(R.color.colorLight));
                    loginGoBtn.setEnabled(true);
                } else {
                    loginGoBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_btn_theme_pale_circle));
                    loginGoBtn.setTextColor(getResources().getColor(R.color.colorDark));
                    loginGoBtn.setEnabled(false);
                }
            }
        });
    }
}
