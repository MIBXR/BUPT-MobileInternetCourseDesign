package com.tsukiyoumi.myqrnote;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tsukiyoumi.myqrnote.util.StaticUtils;
import com.tsukiyoumi.myqrnote.util.PathUtils;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class ScanActivity extends AppCompatActivity {
    private FloatingActionButton mOpenAlbum;
    private ZXingView scanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        init();
        initListener();
    }

    private void init() {
        mOpenAlbum = findViewById(R.id.openAlbum);
        scanView = findViewById(R.id.scanView);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int fabColor = sharedPreferences.getInt("fabColor", getResources().getColor(R.color.colorPrimary));
        mOpenAlbum.setBackgroundTintList(ColorStateList.valueOf(fabColor));
    }

    private void initListener() {
        scanView.setDelegate(new QRCodeView.Delegate() {
            @Override
            public void onScanQRCodeSuccess(String result) {
                vibrate(); // 震动
                Bundle bundle = new Bundle();
                bundle.putString("result", result);
                Intent intent = new Intent();
                intent.putExtra("scanResult", bundle);
                setResult(Activity.RESULT_OK, intent);
//                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCameraAmbientBrightnessChanged(boolean isDark) {

            }

            @Override
            public void onScanQRCodeOpenCameraError() {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        scanView.startCamera();//打开相机
        scanView.showScanRect();//显示扫描框
        scanView.startSpot();//开始识别二维码
    }

    @Override
    protected void onStop() {
        scanView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        scanView.onDestroy();
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    private void openSysAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, StaticUtils.REQUEST_SELECT_IMAGE);
    }

    public void on_openAlbum(View view) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getBaseContext(), "存储权限未授予", Toast.LENGTH_SHORT).show();
        } else {
            openSysAlbum();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case StaticUtils.REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    scanView.post(new Runnable() {
                        @Override
                        public void run() {
                            scanView.decodeQRCode(PathUtils.getFilePathByUri(getBaseContext(), data.getData()));
                        }
                    });
                    break;
                }
        }
    }
}
