package com.tsukiyoumi.myqrnote;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.tsukiyoumi.myqrnote.paper.PaperOnboardingEngine;
import com.tsukiyoumi.myqrnote.paper.PaperOnboardingPage;
import com.tsukiyoumi.myqrnote.paper.listeners.PaperOnboardingOnChangeListener;
import com.tsukiyoumi.myqrnote.paper.listeners.PaperOnboardingOnRightOutListener;

import java.util.ArrayList;

public class PaperOnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_onboarding);

        PaperOnboardingEngine engine = new PaperOnboardingEngine(findViewById(R.id.onboardingRootView), getDataForOnboarding(), getApplicationContext());

        engine.setOnChangeListener(new PaperOnboardingOnChangeListener() {
            @Override
            public void onPageChanged(int oldElementIndex, int newElementIndex) {
//                Toast.makeText(getApplicationContext(), "Swiped from " + oldElementIndex + " to " + newElementIndex, Toast.LENGTH_SHORT).show();
            }
        });

        engine.setOnRightOutListener(new PaperOnboardingOnRightOutListener() {
            @Override
            public void onRightOut() {
                // Probably here will be your exit action
//                Toast.makeText(getApplicationContext(), "Swiped out right", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private ArrayList<PaperOnboardingPage> getDataForOnboarding() {
        // prepare data
        PaperOnboardingPage scr1 = new PaperOnboardingPage("笔记 OR 提醒",
                "普通笔记, 或是需要通知提醒,\n都能为您准确记录, 准时提醒",
                Color.parseColor("#83A6FE"),
                R.mipmap.p1,
                R.drawable.feature);
        PaperOnboardingPage scr2 = new PaperOnboardingPage("悬浮按钮",
                "一键创建, 随心所欲",
                Color.parseColor("#CFC199"),
                R.mipmap.p2,
                R.drawable.feature);
        PaperOnboardingPage scr3 = new PaperOnboardingPage("设置标签",
                "多样标签, 选择丰富, 还可自定义标签\n让笔记分类有条不紊",
                Color.parseColor("#83A6FE"),
                R.mipmap.p3,
                R.drawable.feature);
        PaperOnboardingPage scr4 = new PaperOnboardingPage("支持搜索",
                "精准搜索, 定位笔记 or 提醒",
                Color.parseColor("#CFC199"),
                R.mipmap.p4,
                R.drawable.feature);
        PaperOnboardingPage scr5 = new PaperOnboardingPage("三大特色功能",
                "OCR文字识别,\n二维码生成与分享,\n二维码快速导入\n加快您的记录速度, 也方便与他人分享",
                Color.parseColor("#83A6FE"),
                R.mipmap.p5,
                R.drawable.feature);
        PaperOnboardingPage scr6 = new PaperOnboardingPage("丰富设置, 还可登录账号",
                "满足不同场景的需求",
                Color.parseColor("#CFC199"),
                R.mipmap.p6,
                R.drawable.feature);
        PaperOnboardingPage scr6_5 = new PaperOnboardingPage("长按",
                "有时候长按会有隐藏功能哦~",
                Color.parseColor("#83A6FE"),
                R.mipmap.p6_5,
                R.drawable.feature);
        PaperOnboardingPage scr6_5_5 = new PaperOnboardingPage("深色",
                "适配深色模式, 呵护你的眼睛",
                Color.parseColor("#CFC199"),
                R.mipmap.p6_5_5,
                R.drawable.feature);
        PaperOnboardingPage scr7 = new PaperOnboardingPage("国区特供启动界面",
                "#广告位招租\n( 可以关闭 )",
                Color.parseColor("#83A6FE"),
                R.mipmap.p7,
                R.drawable.feature);
        PaperOnboardingPage scr8 = new PaperOnboardingPage("使用愉快 !",
                "左滑结束引导\n( 可以在设置中反复查看 )",
                Color.parseColor("#CFC199"),
                R.mipmap.p8,
                R.drawable.feature);

        ArrayList<PaperOnboardingPage> elements = new ArrayList<>();
        elements.add(scr1);
        elements.add(scr2);
        elements.add(scr3);
        elements.add(scr4);
        elements.add(scr5);
        elements.add(scr6);
        elements.add(scr6_5);
        elements.add(scr6_5_5);
        elements.add(scr7);
        elements.add(scr8);
        return elements;
    }

    public void paper_skip(View view) {
        finish();
    }
}