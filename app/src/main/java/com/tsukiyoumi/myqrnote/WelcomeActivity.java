package com.tsukiyoumi.myqrnote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity {
    private TextView appName;

    public static Handler mhandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    private Runnable mrunnable = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        init();

        initPrefs();
    }

    private void init() {
        appName = findViewById(R.id.appName);
        appName.setTypeface(appName.getTypeface(), Typeface.ITALIC);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean("dontWelcome", false)) {
            mhandler.postDelayed(mrunnable, 2000);
        }
        else {
            startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
            finish();
        }
    }

    public void hello_skip(View view) {
        mhandler.removeCallbacks(mrunnable);
        startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
        finish();
    }

    private void initPrefs() {
        SharedPreferences sharedPreferences;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!sharedPreferences.contains("firstOpen")) {
            editor.putBoolean("firstOpen", true);
            editor.commit();
        }
    }

}
