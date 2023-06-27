package com.tsukiyoumi.myqrnote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created on 2021/6/5.
 * @author mibxr@bupt.edu.cn (Xie Rui)
 *
 * Edited on 2021/6/6.
 * @editer mibxr@bupt.edu.cn (Xie Rui)
 *
 * 用于初期单元测试
 */

public class UnitTestActivity extends AppCompatActivity implements View.OnClickListener{
    private Button uibtn0;
    private Button uibtn1;
    private Button uibtn2;
    private Button uibtn3;
    private Button uibtn4;
    private Button uibtn5;
    private Button uibtn6;
    private Button uibtn8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_test);

        uibtn0 = findViewById(R.id.btn0);
        uibtn0.setOnClickListener(this);
        uibtn1 = findViewById(R.id.btn1);
        uibtn1.setOnClickListener(this);
        uibtn2 = findViewById(R.id.btn2);
        uibtn2.setOnClickListener(this);
        uibtn3 = findViewById(R.id.btn3);
        uibtn3.setOnClickListener(this);
        uibtn4 = findViewById(R.id.btn4);
        uibtn4.setOnClickListener(this);
        uibtn5 = findViewById(R.id.btn5);
        uibtn5.setOnClickListener(this);
        uibtn6 = findViewById(R.id.btn6);
        uibtn6.setOnClickListener(this);
        uibtn8 = findViewById(R.id.btn8);
        uibtn8.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn0:
                startActivity(new Intent(this, HomeActivity.class));
                break;
            case R.id.btn1:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.btn2:
                startActivity(new Intent(this, ResultActivity.class));
                break;
            case R.id.btn3:
                startActivity(new Intent(this, ScanActivity.class));
                break;
//            case R.id.btn4:
//                startActivity(new Intent(this, UserCenterActivity.class));
//                break;
            case R.id.btn5:
                startActivity(new Intent(this, EditActivity.class));
                break;
            case R.id.btn6:
                startActivity(new Intent(this, EditAlarmActivity.class));
                break;
            case R.id.btn8:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            default:
                break;
        }
    }
}