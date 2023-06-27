package com.tsukiyoumi.myqrnote;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dou361.dialogui.DialogUIUtils;
import com.tsukiyoumi.myqrnote.baidu.OCRapi;
import com.tsukiyoumi.myqrnote.notes.Plan;
import com.tsukiyoumi.myqrnote.util.ImageUtil;
import com.tsukiyoumi.myqrnote.util.PathUtils;
import com.tsukiyoumi.myqrnote.util.QRCodeUtil;
import com.tsukiyoumi.myqrnote.util.StaticUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditAlarmActivity extends BaseActivity implements View.OnClickListener {

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;
    private EditText et_title;
    private static EditText et;
    private Button set_date;
    private Button set_time;



    private TextView date;
    private TextView time;
    private Plan plan;
    private int[] dateArray = new int[3];
    private int[] timeArray = new int[2];

    private int openMode = 0;
    private String old_title = "";
    private String old_content = "";
    private String old_time = "";
    private long id = 0;
    private boolean timeChange = false;

    private Dialog dialog;

    public static Handler mhandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticUtils.WHAT_OCR_RESULT:
                    et.setText(msg.getData().getString("OCRdata"));
//                    Toast.makeText(getBaseContext(),"handler收到msg, what = 0", Toast.LENGTH_SHORT).show();
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
                result = OCRapi.OCRapi(filepath);
                dialog.dismiss();
            }
            else {
                Toast.makeText(EditAlarmActivity.this, "没有选择图片啊", Toast.LENGTH_SHORT).show();
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
        new EditAlarmActivity.mthread(path).start();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();

        Intent getIntent = getIntent();
        openMode = getIntent.getIntExtra("mode", 0);
        if(openMode == 1){  // 打开已存在的Plan
            id = getIntent.getLongExtra("id", 0);
            old_title = getIntent.getStringExtra("title");
            old_content = getIntent.getStringExtra("content");
            old_time = getIntent.getStringExtra("time");
            et_title.setText(old_title);
            et_title.setSelection(old_title.length());
            et.setText(old_content);
            et.setSelection(old_content.length());

            String[] wholeTime = old_time.split(" ");
            String[] temp = wholeTime[0].split("-");
            String[] temp1 = wholeTime[1].split(":");
            setDateTV(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]));
            setTimeTV(Integer.parseInt(temp1[0]), Integer.parseInt(temp1[1]));
        }

        myToolbar.setNavigationIcon(this.getResources().getDrawable(R.drawable.ic_keyboard_arrow_left_white_24dp));

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!canBeSet()) {
                    if (openMode == 2) {
                        Toast.makeText(EditAlarmActivity.this, R.string.edit_time_past, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Intent intent = new Intent();
                        intent.putExtra("mode", 11);   // 修改Plan
                        intent.putExtra("title", et_title.getText().toString());
                        intent.putExtra("content", et.getText().toString());
                        intent.putExtra("time", date.getText().toString() + " " + time.getText().toString());
                        intent.putExtra("id", id);
                        setResult(RESULT_OK, intent);
                        finish();
                        overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
                    }
                }
                else if (et.getText().toString().length() + et_title.getText().toString().length() == 0 && openMode == 2){  // 新Plan
                    Intent intent1 = new Intent();
                    intent1.putExtra("mode", -1);   // 未新建
                    setResult(RESULT_OK, intent1);
                    finish();
                    overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
                }
                else if (et_title.getText().toString().length() == 0) {
                    Toast.makeText(EditAlarmActivity.this, R.string.edit_title_empty, Toast.LENGTH_SHORT).show();
                }
                else {
                    isTimeChange();
                    Intent intent = new Intent();
                    if (openMode == 2) {
                        intent.putExtra("mode", 10);   // 新建Plan
                        intent.putExtra("title", et_title.getText().toString());
                        intent.putExtra("content", et.getText().toString());
                        intent.putExtra("time", date.getText().toString() + " " + time.getText().toString());
                        Log.d(TAG, date.getText().toString() + time.getText().toString());
                    }
                    else {
                        if (et.getText().toString().equals(old_content) && et_title.getText().toString().equals(old_title) && !timeChange) {
                            intent.putExtra("mode", -1);  // 未修改
                        }
                        else {
                            intent.putExtra("mode", 11);   // 修改Plan
                            intent.putExtra("title", et_title.getText().toString());
                            intent.putExtra("content", et.getText().toString());
                            intent.putExtra("time", date.getText().toString() + " " + time.getText().toString());
                            intent.putExtra("id", id);
                        }
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
                }
            }
        });

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( keyCode== KeyEvent.KEYCODE_HOME){
            return true;
        }
        else if( keyCode== KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if(!canBeSet()) {
                if (openMode == 2) {
                    Toast.makeText(EditAlarmActivity.this, R.string.edit_time_past, Toast.LENGTH_SHORT).show();
                    return false;
                }
                else {
                    Intent intent = new Intent();
                    intent.putExtra("mode", 11);   // 修改Plan
                    intent.putExtra("title", et_title.getText().toString());
                    intent.putExtra("content", et.getText().toString());
                    intent.putExtra("time", date.getText().toString() + " " + time.getText().toString());
                    intent.putExtra("id", id);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
                    return true;
                }
            }
            else if(et.getText().toString().length() + et_title.getText().toString().length() == 0 && openMode == 2){
                Intent intent1 = new Intent();
                intent1.putExtra("mode", -1);
                setResult(RESULT_OK, intent1);
                finish();
                overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
                return true;
            }
            else if (et_title.getText().toString().length() == 0) {
                Toast.makeText(EditAlarmActivity.this, R.string.edit_title_empty, Toast.LENGTH_SHORT).show();
                return false;
            }
            else {
                isTimeChange();
                Intent intent = new Intent();
                if (openMode == 2) {
                    intent.putExtra("mode", 10);
                    intent.putExtra("title", et_title.getText().toString());
                    intent.putExtra("content", et.getText().toString());
                    intent.putExtra("time", date.getText().toString() + " " + time.getText().toString());
                    Log.d(TAG, date.getText().toString() + time.getText().toString());
                }
                else {
                    if (et.getText().toString().equals(old_content) && et_title.getText().toString().equals(old_title) && !timeChange) {
                        intent.putExtra("mode", -1);
                    }
                    else {
                        intent.putExtra("mode", 11);
                        intent.putExtra("title", et_title.getText().toString());
                        intent.putExtra("content", et.getText().toString());
                        intent.putExtra("time", date.getText().toString() + " " + time.getText().toString());
                        intent.putExtra("id", id);
                    }
                }
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
                return true;
            }
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
                new AlertDialog.Builder(EditAlarmActivity.this)
                        .setMessage(R.string.edit_delete_confirm)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(openMode == 2){
                                    intent.putExtra("mode", -1);
                                    setResult(RESULT_OK, intent);
                                }
                                else {
                                    intent.putExtra("mode", 12);
                                    intent.putExtra("id", id);
                                    setResult(RESULT_OK, intent);
                                }
                                finish();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
            case R.id.scan:
                startActivityForResult(new Intent(EditAlarmActivity.this, ScanActivity.class),
                        StaticUtils.REQUEST_CODE_SCANIN);
                break;
            case R.id.share:
                String share_content = "PlanByXR_&%#_" +
                        date.getText().toString() + " " + time.getText().toString() + "_&%#_" +
                        et_title.getText() + "_&%#_" +
                        et.getText();
                if (et_title.getText().length() <= 0) {
                    Toast.makeText(EditAlarmActivity.this, R.string.empty_title, Toast.LENGTH_SHORT).show();
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

    private void init(){
        plan = new Plan();
        dateArray[0] = plan.getYear();
        dateArray[1] = plan.getMonth() + 1;
        dateArray[2] = plan.getDay();
        timeArray[0] = plan.getHour();
        timeArray[1] = plan.getMinute();

        et_title = findViewById(R.id.et_title);
        et = findViewById(R.id.et);
        set_date = findViewById(R.id.set_date);
        set_time = findViewById(R.id.set_time);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);

        setDateTV(dateArray[0], dateArray[1], dateArray[2]);
        setTimeTV((timeArray[1]>54? timeArray[0]+1 : timeArray[0]), (timeArray[1]+5)%60);
        Log.d(TAG, "init: "+dateArray[1]);

        set_date.setOnClickListener(this);
        set_time.setOnClickListener(this);

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                setDateTV(year, month+1, dayOfMonth);
            }
        };
        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                setTimeTV(hourOfDay, minute);
            }
        };
    }

    private void setDateTV(int y, int m, int d){
        String temp = y + "-";
        if(m<10) temp += "0";
        temp += (m + "-");
        if(d<10) temp +="0";
        temp += d;
        date.setText(temp);
        dateArray[0] = y;
        dateArray[1] = m;
        dateArray[2] = d;
    }

    private void setTimeTV(int h, int m){
        String temp = "";
        if(h<10) temp += "0";
        temp += (h + ":");
        if(m<10) temp += "0";
        temp += m;
        time.setText(temp);
        timeArray[0] = h;
        timeArray[1] = m;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_date:
                DatePickerDialog dialog = new DatePickerDialog(EditAlarmActivity.this,
                        R.style.DayDialogTheme, dateSetListener,
                        dateArray[0], dateArray[1] - 1, dateArray[2]);
                dialog.show();
                break;
            case R.id.set_time:
                TimePickerDialog dialog1 = new TimePickerDialog(EditAlarmActivity.this,
                        R.style.DayDialogTheme, timeSetListener,
                        timeArray[0], timeArray[1], true);
                dialog1.show();
                break;
        }
    }
    
    private void isTimeChange(){
        String newTime = date.getText().toString() + " " + time.getText().toString();
        if(!newTime.equals(old_time)) timeChange = true;
    }

    private boolean canBeSet(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateArray[0], dateArray[1] - 1, dateArray[2], timeArray[0], timeArray[1]);
        Calendar cur = Calendar.getInstance();
        if(cur.before(calendar)) return true;
        else return false;
    }

    private void imgChooseDialog(Bitmap qrcode_bitmap){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditAlarmActivity.this);
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
        String fileName = "qr_"+System.currentTimeMillis() + ".jpg";
        boolean isSaveSuccess = ImageUtil.saveImageToGallery(EditAlarmActivity.this, bitmap,fileName);
        if (isSaveSuccess) {
            Toast.makeText(EditAlarmActivity.this, "图片已保存至本地", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(EditAlarmActivity.this, "保存图片失败，请稍后重试", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StaticUtils.REQUEST_CODE_SCANIN && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getBundleExtra("scanResult");
            String result = bundle.getString("result", "无结果");
            String[] split_result = result.split("_&%#_");
//            for (int i = 0; i < split_result.length; i++)
//                Log.d("scan", split_result[i]);
            if (split_result.length > 0) {
                if (split_result[0].equals("PlanByXR")) {
                    et_title.setText(split_result[2]);
                    if (split_result.length == 4)
                        et.setText(split_result[3]);
                }
                else if (split_result[0].equals("NoteByXR")) {
                    Toast.makeText(EditAlarmActivity.this, R.string.share_type_not_plan, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("mode", 0);   // 新建Note
                    intent.putExtra("content", split_result[1]);
                    intent.putExtra("time", dateToStr());
                    intent.putExtra("loc", "无位置信息");
                    intent.putExtra("tag", 0);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
                }
                else {
                    Toast.makeText(EditAlarmActivity.this, R.string.share_not_self, Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (requestCode == StaticUtils.REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            dialog = DialogUIUtils.showLoading(EditAlarmActivity.this,
                    "识别中...", false, false,
                    false, true)
                    .show();
            start_thread(PathUtils.getFilePathByUri(getBaseContext(), data.getData()));
        }
    }

    public String dateToStr(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return simpleDateFormat.format(date);
    }
}
