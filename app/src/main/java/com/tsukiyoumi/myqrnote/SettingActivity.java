package com.tsukiyoumi.myqrnote;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.dou361.dialogui.DialogUIUtils;
import com.google.gson.Gson;
import com.suke.widget.SwitchButton;
import com.tsukiyoumi.myqrnote.entity.AuthEntity;
import com.tsukiyoumi.myqrnote.http.Api;
import com.tsukiyoumi.myqrnote.http.ApiListener;
import com.tsukiyoumi.myqrnote.http.ApiUtil;
import com.tsukiyoumi.myqrnote.http.UniteApi;
import com.tsukiyoumi.myqrnote.util.StaticUtils;

import java.util.HashMap;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class SettingActivity extends BaseActivity {
    private SwitchButton reverseSort;
    private SwitchButton noteTitle;
    private SwitchButton dontBother;
    private SwitchButton dontWelcome;
    private SharedPreferences sharedPreferences;

    private Dialog dialog;
    private ImageView scanIcon;
    private ImageView headIcon;
    private TextView userNameTxt;
    private TextView cardV1T;
    private View cardV2;
    private Button logInOut;
    private LinearLayout reRead;

    private boolean need_fresh = false;
    private boolean need_cancel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        initView();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myToolbar.setNavigationIcon(this.getResources().getDrawable(R.drawable.ic_keyboard_arrow_left_white_24dp));

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (need_fresh){
                    intent.putExtra("need_fresh", true);  // 需要更新界面
                }
                else {
                    intent.putExtra("need_fresh", false);  // 不需要更新界面
                }
                if (need_cancel) {

                }
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
            }
        });

        scanIcon = findViewById(R.id.scanLogin);
        scanIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("accountState", MODE_PRIVATE);
                boolean isPermit = preferences.getBoolean("isPermit", false);
                if (isPermit) {
                    startActivityForResult(new Intent(SettingActivity.this, ScanActivity.class),
                            StaticUtils.REQUEST_CODE_SCANIN);
                } else {
                    Toast.makeText(getBaseContext(), "请先登录再使用扫一扫", Toast.LENGTH_SHORT).show();
                }
            }
        });
        headIcon = findViewById(R.id.headIcon);
        userNameTxt = findViewById(R.id.userName);
        cardV1T = findViewById(R.id.card1Text);
        cardV2 = findViewById(R.id.card2);
        logInOut = findViewById(R.id.userLoginBtn);
        logInOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("accountState", MODE_PRIVATE);
                boolean isPermit = preferences.getBoolean("isPermit", false);
                if (isPermit) {
                    SharedPreferences.Editor editor = getSharedPreferences("accountState", MODE_PRIVATE).edit();
                    editor.putBoolean("isPermit", false);
                    editor.commit();
                    refresh2unloggin();
                } else {
                    startActivityForResult(new Intent(SettingActivity.this, LoginActivity.class),
                            StaticUtils.REQUEST_CODE_LOGIN);
                }

            }
        });

        SharedPreferences preferences = getSharedPreferences("accountState", MODE_PRIVATE);
        boolean isPermit = preferences.getBoolean("isPermit", false);
        if (isPermit) {
            refresh2loggined();
        } else {
            refresh2unloggin();
        }
    }

    private void initView(){
        reverseSort = findViewById(R.id.reverseSort);
        noteTitle = findViewById(R.id.noteTitle);
        dontBother = findViewById(R.id.dont_bother);
        dontWelcome = findViewById(R.id.dont_welcome);
        reRead = findViewById(R.id.re_read_paper);

        reverseSort.setChecked(sharedPreferences.getBoolean("reverseSort", true));
        reverseSort.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = sharedPreferences1.edit();
                editor.putBoolean("reverseSort", isChecked);
                need_fresh = true;
                editor.commit();
            }
        });

        noteTitle.setChecked(sharedPreferences.getBoolean("noteTitle", true));
        noteTitle.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = sharedPreferences1.edit();
                editor.putBoolean("noteTitle", isChecked);
                need_fresh = true;
                editor.commit();
            }
        });

        dontBother.setChecked(sharedPreferences.getBoolean("dontBother", false));
        dontBother.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = sharedPreferences1.edit();
                editor.putBoolean("dontBother", isChecked);
                need_cancel = true;
                editor.commit();
            }
        });

        dontWelcome.setChecked(sharedPreferences.getBoolean("dontWelcome", false));
        dontWelcome.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = sharedPreferences1.edit();
                editor.putBoolean("dontWelcome", isChecked);
                editor.commit();
            }
        });

        reRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, PaperOnboardingActivity.class));
            }
        });
    }

    // 手机键盘（按键）事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if( keyCode== KeyEvent.KEYCODE_HOME){
            return true;
        } else if( keyCode== KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            Intent intent = new Intent();
            if(need_fresh){
                intent.putExtra("need_fresh", true);  // 需要更新界面
            }
            else {
                intent.putExtra("need_fresh", false);  // 不需要更新界面
            }
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StaticUtils.REQUEST_CODE_LOGIN && resultCode == Activity.RESULT_OK) {
            refresh2loggined();
        }
        else if (requestCode == StaticUtils.REQUEST_CODE_SCANIN && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getBundleExtra("scanResult");
            String result = bundle.getString("result", "无结果");

            dialog = DialogUIUtils.showLoading(this,
                    "处理中...", false, false,
                    false, true)
                    .show();
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("userId", ApiUtil.USER_ID);
            hashMap.put("isScan", "true");

            new UniteApi(ApiUtil.TOKEN_INFO + result, hashMap).post(new ApiListener() {
                @Override
                public void success(Api api) {
                    dialog.dismiss();
                    UniteApi uniteApi = (UniteApi) api;
                    Gson gson = new Gson();
                    AuthEntity auth = gson.fromJson(uniteApi.getJsonData().toString(), AuthEntity.class);
                    if (auth.getAuthState() == 0 || auth.getAuthState() == 2) {
                        Intent intent = new Intent(SettingActivity.this, ResultActivity.class);
                        intent.putExtra("auth", auth);
                        startActivity(intent);
                    } else if (auth.getAuthState() == 1) {
                        Toast.makeText(getBaseContext(), "登录码已使用", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getBaseContext(), "登录码已过期", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void failure(Api api) {
                    dialog.dismiss();
                    Toast.makeText(getBaseContext(), "服务器错误", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void refresh2loggined() {
        boolean isPermit = getBaseContext().getSharedPreferences("accountState", Context.MODE_PRIVATE).getBoolean("isPermit", false);
        if (isPermit) {
            Resources resources = this.getResources();
            String userName = getBaseContext().getSharedPreferences("accountState", Context.MODE_PRIVATE).getString("userName", "测试用户");
            String userAvatar = getBaseContext().getSharedPreferences("accountState", Context.MODE_PRIVATE).getString("userAvatar", "");
            userNameTxt.setText(userName);
            if (userAvatar == "") {
                Glide.with(this)
                        .load(userAvatar)
                        .apply(bitmapTransform(new CircleCrop()))
                        .into(headIcon);
            }
            else {
                headIcon.setImageResource(R.drawable.default_head);
            }
            cardV1T.setText("登录用户");
            cardV2.setVisibility(View.GONE);
            logInOut.setText("退出登录");
            logInOut.setBackground(resources.getDrawable(R.drawable.bg_btn_theme_pale_circle));
            logInOut.setTextColor(resources.getColor(R.color.colorText));
        }
    }
    public void refresh2unloggin() {
        userNameTxt.setText("未登录");
        headIcon.setImageResource(R.drawable.default_head);
        cardV1T.setText("游客");
        cardV2.setVisibility(View.VISIBLE);
        logInOut.setText("登录");
        Resources resources = this.getResources();
        logInOut.setBackground(resources.getDrawable(R.drawable.bg_btn_theme_circle));
        logInOut.setTextColor(resources.getColor(R.color.colorLight));
    }
}