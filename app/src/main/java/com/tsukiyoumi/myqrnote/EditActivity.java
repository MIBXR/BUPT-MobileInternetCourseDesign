package com.tsukiyoumi.myqrnote;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dou361.dialogui.DialogUIUtils;
import com.tsukiyoumi.myqrnote.baidu.OCRapi;
import com.tsukiyoumi.myqrnote.db.NoteDatabase;
import com.tsukiyoumi.myqrnote.db.PlanDatabase;
import com.tsukiyoumi.myqrnote.http.ApiUtil;
import com.tsukiyoumi.myqrnote.util.ImageUtil;
import com.tsukiyoumi.myqrnote.util.PathUtils;
import com.tsukiyoumi.myqrnote.util.QRCodeUtil;
import com.tsukiyoumi.myqrnote.util.StaticUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EditActivity extends BaseActivity{

    private NoteDatabase dbHelper;
    private Context context = this;

    private static EditText et;
    private Button loc_btn;
    private boolean btn_clicked = false;

    private String old_content = "";
    private String old_time = "";
    private String old_loc = "";
    private int old_Tag = 1;
    private long id = 0;
    private int openMode = 0;
    private int tag = 1;

    private Dialog dialog;

    private LocationManager locationManager;
    private LocationListener listener;
    private boolean isLocating = false;

    public static Handler mhandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticUtils.WHAT_OCR_RESULT:
                    et.setText(msg.getData().getString("OCRdata"));
                    break;
                default:
                    break;
            }
        }
    };

    private class mthread extends Thread {
        private String filepath ="";
        private String result = "";
        public mthread(String path) {
            filepath = path;
        }
        @Override
        public void run() {
            super.run();
            if (!filepath.equals("")) {
                OCRapi api = new OCRapi();
                result = api.OCRapi(filepath);
                dialog.dismiss();
            }
            else {
                Toast.makeText(EditActivity.this, "没有选择图片", Toast.LENGTH_SHORT).show();
            }

            Message message =Message.obtain();
            message.what = StaticUtils.WHAT_OCR_RESULT;
            Bundle bundle = new Bundle();
            bundle.putString("OCRdata",result);
            message.setData(bundle);
            mhandler.sendMessage(message);
        }
    }
    public void start_thread(String path) {
        new mthread(path).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Spinner mySpinner = (Spinner)findViewById(R.id.spinner);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", "").split("_")); //获取tags
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, tagList);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);

        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tag = (int)id + 1;
//                Log.d("tagChange", id +"");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        myToolbar.setNavigationIcon(this.getResources().getDrawable(R.drawable.ic_keyboard_arrow_left_white_24dp));

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if(openMode == 4){  // 新Note
                    if(et.getText().toString().length() == 0){
                        intent.putExtra("mode", -1);  // 未新建Note
                    }
                    else{
                        intent.putExtra("mode", 0);   // 新建Note
                        intent.putExtra("content", et.getText().toString());
                        intent.putExtra("time", dateToStr());
                        intent.putExtra("loc", loc_btn.getText().toString());
                        intent.putExtra("tag", tag);
                    }
                }
                else {
                    if (et.getText().toString().equals(old_content) && old_Tag == tag)
                        intent.putExtra("mode", -1);  // 未修改Note
                    else {
                        intent.putExtra("mode", 1);   // 修改Note
                        intent.putExtra("content", et.getText().toString());
                        intent.putExtra("time", dateToStr());
                        intent.putExtra("loc", loc_btn.getText().toString());
                        intent.putExtra("id", id);
                        intent.putExtra("tag", tag);
                    }
                }
                if(isLocating){ //如果正在定位
                    locationManager.removeUpdates(listener);//停止定位
                    isLocating=false;
                }
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
            }
        });

        et = (EditText)findViewById(R.id.et);
        loc_btn = (Button)findViewById(R.id.set_loc);
        loc_btn.setText("无位置信息");

        Intent getIntent = getIntent();

        openMode = getIntent.getIntExtra("mode", 0);
        if (openMode == 3) {  // 打开已存在的note
            id = getIntent.getLongExtra("id", 0);
            old_content = getIntent.getStringExtra("content");
            old_time = getIntent.getStringExtra("time");
            old_loc = getIntent.getStringExtra("loc");
            old_Tag = getIntent.getIntExtra("tag", 1);
            et.setText(old_content);
            et.setSelection(old_content.length());
            loc_btn.setText(old_loc);
            mySpinner.setSelection(old_Tag - 1);
        }
    }

    // 手机键盘（按键）事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if( keyCode== KeyEvent.KEYCODE_HOME){
            return true;
        } else if( keyCode== KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            Intent intent = new Intent();
            if(openMode == 4){  // 新Note
                if(et.getText().toString().length() == 0){
                    intent.putExtra("mode", -1);   // 未新建Note
                }
                else{
                    intent.putExtra("mode", 0);   // 新建Note
                    intent.putExtra("content", et.getText().toString());
                    intent.putExtra("time", dateToStr());
                    intent.putExtra("loc", loc_btn.getText().toString());
                    intent.putExtra("tag", tag);
                }
            }
            else {
                if (et.getText().toString().equals(old_content) && old_Tag == tag)
                    intent.putExtra("mode", -1);  // 未修改Note
                else {
                    intent.putExtra("mode", 1);  // 修改Note
                    intent.putExtra("content", et.getText().toString());
                    intent.putExtra("time", dateToStr());
                    intent.putExtra("loc", loc_btn.getText().toString());
                    intent.putExtra("id", id);
                    intent.putExtra("tag", tag);
                }
            }
            if(isLocating){ //如果正在定位
                locationManager.removeUpdates(listener);//停止定位
                isLocating=false;
            }
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Intent intent = new Intent();
        switch (item.getItemId()){
            case R.id.delete:
                new AlertDialog.Builder(EditActivity.this)
                        .setMessage(R.string.edit_delete_confirm)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(openMode == 4){  // 新Note
                                    intent.putExtra("mode", -1);   // 未新建Note
                                    setResult(RESULT_OK, intent);
                                }
                                else {
                                    intent.putExtra("mode", 2);   // 删除Note
                                    intent.putExtra("id", id);
                                    setResult(RESULT_OK, intent);
                                }
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                break;
            case R.id.scan:
                startActivityForResult(new Intent(EditActivity.this, ScanActivity.class),
                        StaticUtils.REQUEST_CODE_SCANIN);
                break;
            case R.id.share:
                String share_content = "NoteByXR_&%#_" + et.getText();
                if (et.getText().length() <= 0) {
                    Toast.makeText(EditActivity.this, R.string.empty_content, Toast.LENGTH_SHORT).show();
                }
                else {
                    Bitmap qrcode_bitmap = QRCodeUtil.createQRCodeBitmap(share_content, 650, 650, "UTF-8",
                            "L", "3", Color.BLACK, Color.WHITE, null, 0.2F, null);
                    imgChooseDialog(qrcode_bitmap);
                }
                break;
            case R.id.ocr:
                Intent selImgIntent = new Intent(Intent.ACTION_PICK, null);
                selImgIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(selImgIntent, StaticUtils.REQUEST_SELECT_IMAGE);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void imgChooseDialog(Bitmap qrcode_bitmap){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
        builder.setTitle("选择分享方式");
        builder.setIcon(R.drawable.ic_baseline_share_prim_24);
        builder.setItems(new String[]{"存储二维码至手机", "直接发送二维码"}, new DialogInterface.OnClickListener() { // 列表对话框
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                switch (which) {
                    case 0:  // 存储
                        saveImg(qrcode_bitmap);
                        break;
                    case 1:  // 分享
                        shareImg(qrcode_bitmap);
                        break;
                    default:
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveImg(Bitmap bitmap){
        String fileName = "MyQRNote_share_"+System.currentTimeMillis() + ".jpg";
        boolean isSaveSuccess = ImageUtil.saveImageToGallery(EditActivity.this, bitmap,fileName);
        if (isSaveSuccess) {
            Toast.makeText(EditActivity.this, "图片已保存至本地", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(EditActivity.this, "保存图片失败，请稍后重试", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImg(Bitmap bitmap){
        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");//设置分享内容的类型
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent = Intent.createChooser(intent, "分享");
        startActivity(intent);
    }

    public String dateToStr(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return simpleDateFormat.format(date);
    }

    public void set_loc(View view) {
        isLocating = true;
        String provider = LocationManager.GPS_PROVIDER;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Toast.makeText(this, "获取系统服务失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 如果没有打开位置信息开关,则转至位置信息开关设置页面
        if (!locationManager.isProviderEnabled(provider)) {
            (new android.app.AlertDialog.Builder(this))
                    .setTitle("提示")
                    .setMessage("需要启用定位")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        //点击确定后打开设置对话框
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //打开设置页面
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException ex) {
                                intent.setAction(Settings.ACTION_SETTINGS);
                                try {
                                    startActivity(intent);
                                } catch (Exception e) {
                                }
                            }
                        }
                    })
                    .create().show();
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "无定位权限", Toast.LENGTH_SHORT).show();
            return;
        }

        Location position;
        position = locationManager.getLastKnownLocation(provider);
        ShowLocationInfo(position);

        listener=new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onLocationChanged(Location location) {
                ShowLocationInfo(location);
            }
        };
        locationManager.requestLocationUpdates(provider, 2000, 10, listener);
    }

    private void ShowLocationInfo(Location position){
        List<Address> address=null;
        Geocoder gc=new Geocoder(EditActivity.this, Locale.CHINA);
        if (position!=null) {
            try {
                address = gc.getFromLocation(position.getLatitude(), position.getLongitude(), 2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String t="";
        if (address!=null && address.size()>0){
            if (address.get(0).getAddressLine(1) != null)
                t=address.get(0).getAddressLine(1);
            else
                t="暂无位置信息";
        }
        else
            t="暂无位置信息";
        loc_btn.setText(t);
//        Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StaticUtils.REQUEST_CODE_SCANIN && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getBundleExtra("scanResult");
            String result = bundle.getString("result", "无结果");
            String[] split_result = result.split("_&%#_");
            if (split_result.length > 0) {
                if (split_result[0].equals("NoteByXR")) {
                    et.setText(split_result[1]);
                }
                else if (split_result[0].equals("PlanByXR")) {
                    Toast.makeText(EditActivity.this, R.string.share_type_not_note, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("mode", 10);   // 新建Plan
                    intent.putExtra("title", split_result[2]);
                    if (split_result.length == 4)
                        intent.putExtra("content", split_result[3]);
                    else
                        intent.putExtra("content", "");
                    intent.putExtra("time", split_result[1]);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
                }
                else {
                    Toast.makeText(EditActivity.this, R.string.share_not_self, Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (requestCode == StaticUtils.REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            dialog = DialogUIUtils.showLoading(EditActivity.this,
                    "识别中...", false, false,
                    false, true)
                    .show();
            start_thread(PathUtils.getFilePathByUri(getBaseContext(), data.getData()));
        }
    }
}
