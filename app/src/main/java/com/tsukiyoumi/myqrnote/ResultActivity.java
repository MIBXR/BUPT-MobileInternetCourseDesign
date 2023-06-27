package com.tsukiyoumi.myqrnote;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.dou361.dialogui.DialogUIUtils;
import com.tsukiyoumi.myqrnote.entity.AuthEntity;
import com.tsukiyoumi.myqrnote.http.Api;
import com.tsukiyoumi.myqrnote.http.ApiListener;
import com.tsukiyoumi.myqrnote.http.ApiUtil;
import com.tsukiyoumi.myqrnote.http.UniteApi;

import org.json.JSONException;

import java.util.HashMap;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class ResultActivity extends AppCompatActivity {

    private AuthEntity auth;

    private ImageView resultUAvatar;
    private TextView resultUName;
    private TextView resultTime;
    private TextView resultAddress;
    private TextView resultInfo;
    private Button resultConfirmBtn;
    private Button resultCancelBtn;

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        auth = (AuthEntity) getIntent().getSerializableExtra("auth");

        init();
        initListener();
    }

    private void init() {
        resultUAvatar = findViewById(R.id.resultUAvatar);
        resultUName = findViewById(R.id.resultUName);
        resultTime = findViewById(R.id.resultTime);
        resultAddress = findViewById(R.id.resultAddress);
        resultInfo = findViewById(R.id.resultInfo);
        resultConfirmBtn = findViewById(R.id.resultConfirmBtn);
        resultCancelBtn = findViewById(R.id.resultCancelBtn);


        Glide.with(this)
                .load(ApiUtil.USER_AVATAR)
                .apply(bitmapTransform(new CircleCrop()))
                .into(resultUAvatar);
        resultUName.setText(ApiUtil.USER_NAME);
        resultTime.setText(auth.getAuthTime().toString());
        resultAddress.setText(auth.getAuthAddress());
    }

    private void initListener() {
        resultConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = DialogUIUtils.showLoading(ResultActivity.this,
                        "登录中...", false, false,
                        false, true)
                        .show();
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("userId", ApiUtil.USER_ID);
                new UniteApi(ApiUtil.TOKEN_USE + auth.getAuthToken(), hashMap).post(new ApiListener() {
                    @Override
                    public void success(Api api) {
                        dialog.dismiss();
                        UniteApi uniteApi = (UniteApi) api;
                        try {
                            if (uniteApi.getJsonData().getInt("state") == 1) {
                                Toast.makeText(getBaseContext(), "登录成功", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(getBaseContext(), "登录码已过期，请重试", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getBaseContext(), "登录失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(Api api) {
                        dialog.dismiss();
                        Toast.makeText(getBaseContext(), "登录失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        resultCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
