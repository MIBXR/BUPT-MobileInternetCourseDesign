package com.tsukiyoumi.myqrnote;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
//import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.suke.widget.SwitchButton;
import com.tsukiyoumi.myqrnote.db.NoteDatabase;
import com.tsukiyoumi.myqrnote.db.PlanDatabase;
import com.tsukiyoumi.myqrnote.http.OkHttpUtil;
import com.tsukiyoumi.myqrnote.notes.AlarmReceiver;
import com.tsukiyoumi.myqrnote.notes.Note;
import com.tsukiyoumi.myqrnote.notes.NoteAdapter;
import com.tsukiyoumi.myqrnote.notes.NoteCRUD;
import com.tsukiyoumi.myqrnote.notes.Plan;
import com.tsukiyoumi.myqrnote.notes.PlanAdapter;
import com.tsukiyoumi.myqrnote.notes.PlanCRUD;
import com.tsukiyoumi.myqrnote.notes.TagAdapter;
import com.tsukiyoumi.myqrnote.util.StaticUtils;

import com.dou361.dialogui.DialogUIUtils;

import km.lmy.searchview.SearchView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;

public class HomeActivity extends BaseActivity implements OnItemClickListener, OnItemLongClickListener {

    private NoteDatabase dbHelper;
    private PlanDatabase planDbHelper;

    private FloatingActionButton fab;
    private ListView lv;
    private ListView lv_plan;
    private RelativeLayout lv_layout;
    private RelativeLayout lv_plan_layout;

    private Context context = this;
    private NoteAdapter adapter;
    private PlanAdapter planAdapter;
    private List<Note> noteList = new ArrayList<Note>();
    private List<Plan> planList = new ArrayList<Plan>();
    private TextView mEmptyViewN;
    private TextView mEmptyViewP;

    private Toolbar myToolbar;
    private SearchView mSearchView;

    private PopupWindow popupWindow;  // 左侧弹出菜单
    private PopupWindow popupCover;  // 菜单蒙版
    private LayoutInflater layoutInflater;
    private RelativeLayout main;
    private ViewGroup customView;
    private ViewGroup coverView;
    private WindowManager wm;
    private DisplayMetrics metrics;
    private TagAdapter tagAdapter;

    private CardView setting_card;
    private CardView show_all_card;
    private ListView lv_tag;
    private CardView add_tag;

    private Achievement achievement;

    private SharedPreferences sharedPreferences;
    private SwitchButton content_switch;

    private AlarmManager alarmManager;

    String[] list_String;

    public int MAX_TAG_NUM = 15;

    public static Handler mhandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 权限申请
        initPermission();
        // 初始化通讯
        OkHttpUtil.init();
        // 初始化弹框
        DialogUIUtils.init(this);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        achievement = new Achievement(context);

        //初始化
        init();

        myToolbar.setNavigationIcon(this.getResources().getDrawable(R.drawable.ic_menu_white_24dp));

        if (sharedPreferences.getBoolean("content_switch", false))
            myToolbar.setTitle(R.string.all_plans);
        else
            myToolbar.setTitle(R.string.all_notes);

        myToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUpWindow();
            }
        });
    }

    public void initPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION},
                    StaticUtils.REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == StaticUtils.REQUEST_PERMISSION) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.permi_not_grant)
                            .setMessage(R.string.permi_please)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            initPermission();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getBaseContext(), R.string.permi_not_grant, Toast.LENGTH_SHORT).show();
                        }
                    }).show();
                    break;
                }
            }
        }
    }

    public void init() {
        initPrefs();

        if (sharedPreferences.getBoolean("firstOpen", true)) {
            startActivity(new Intent(HomeActivity.this, PaperOnboardingActivity.class));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstOpen", false);
            editor.commit();
        }

        list_String = new String[]{
                getResources().getString(R.string.before_one_month),
                getResources().getString(R.string.before_three_months),
                getResources().getString(R.string.before_six_months),
                getResources().getString(R.string.before_one_year)
        };

        fab = findViewById(R.id.fab);
        lv = findViewById(R.id.lv);
        lv_plan = findViewById(R.id.lv_plan);
        lv_layout = findViewById(R.id.lv_layout);
        lv_plan_layout = findViewById(R.id.lv_plan_layout);
        content_switch = findViewById(R.id.content_switch);
        myToolbar = findViewById(R.id.my_toolbar);
        mSearchView = findViewById(R.id.searchView);
        mSearchView.setNewHistoryList(Arrays.asList("1","2","3"));
        refreshLvVisibility();

        mEmptyViewN = findViewById(R.id.emptyViewN);
        mEmptyViewP = findViewById(R.id.emptyViewP);

        if (sharedPreferences.getBoolean("content_switch", false)) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_flag_white_24dp));
        }
        else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_white_24dp));
        }

        adapter = new NoteAdapter(getApplicationContext(), noteList);
        planAdapter = new PlanAdapter(getApplicationContext(), planList);

        refreshListView();
        lv.setAdapter(adapter);
        lv.setEmptyView(mEmptyViewN);  // 绑定ListView与Empty TextView
        lv_plan.setAdapter(planAdapter);
        lv_plan.setEmptyView(mEmptyViewP);  // 绑定ListView与Empty TextView

        boolean temp = sharedPreferences.getBoolean("content_switch", false);
        content_switch.setChecked(temp);  // 判断展示Note还是Plan
        content_switch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("content_switch" ,isChecked);
                editor.commit();
                refreshListView();
                refreshLvVisibility();
                if (isChecked) {
//                    mCalender.setVisible(true);
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_flag_white_24dp));
                }
                else {
//                    mCalender.setVisible(false);
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_white_24dp));
//                    fab.setVisibility(View.VISIBLE);
                }
            }
        });

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (content_switch.isChecked()) {
                    Intent intent = new Intent(HomeActivity.this, EditAlarmActivity.class);
                    intent.putExtra("mode", 2);  // 新Plan
                    startActivityForResult(intent, StaticUtils.REQUEST_EDIT);
                    overridePendingTransition(R.anim.in_righttoleft, R.anim.no);
                }
                else {
                    Intent intent = new Intent(HomeActivity.this, EditActivity.class);
                    intent.putExtra("mode", 4);  // 新Note
                    startActivityForResult(intent, StaticUtils.REQUEST_EDIT);
                    overridePendingTransition(R.anim.in_righttoleft, R.anim.out_righttoleft);
                }
            }
        });

        lv.setOnItemClickListener(this);
        lv_plan.setOnItemClickListener(this);

        lv.setOnItemLongClickListener(this);
        lv_plan.setOnItemLongClickListener(this);

        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 搜索相关功能
        mSearchView.setOnInputTextChangeListener(new SearchView.OnInputTextChangeListener() {
            @Override
            public void onTextChanged(CharSequence charSequence) {
                String newText = charSequence.toString();
                if(content_switch.isChecked()) planAdapter.getFilter().filter(newText);
                else adapter.getFilter().filter(newText);
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence) {
                String newText = charSequence.toString();
                if(content_switch.isChecked()) planAdapter.getFilter().filter(newText);
                else adapter.getFilter().filter(newText);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mSearchView.setOnSearchActionListener(new SearchView.OnSearchActionListener() {
            @Override
            public void onSearchAction(String searchText) {
                if(content_switch.isChecked()) planAdapter.getFilter().filter(searchText);
                else adapter.getFilter().filter(searchText);
            }
        });
        mSearchView.setOnSearchBackIconClickListener(new SearchView.OnSearchBackIconClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        initPopupView();
    }

    // 设定程序所需的SharedPreferences默认值
    private void initPrefs() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!sharedPreferences.contains("reverseSort")) {
            editor.putBoolean("reverseSort", false);
            editor.commit();
        }
        if (!sharedPreferences.contains("fabColor")) {
            editor.putInt("fabColor", getResources().getColor(R.color.colorPrimary));
            editor.commit();
        }
        if (!sharedPreferences.contains("tagListString")) {
            String s = "默认_生活_学习_工作_娱乐";
            editor.putString("tagListString", s);
            editor.commit();
        }
        if(!sharedPreferences.contains("content_switch")) {
            editor.putBoolean("content_switch", false);
            editor.commit();
        }
        if(!sharedPreferences.contains("noteTitle")){
            editor.putBoolean("noteTitle", true);
            editor.commit();
        }
        if(!sharedPreferences.contains("dontBother")){
            editor.putBoolean("dontBother", false);
            editor.commit();
        }
        if(!sharedPreferences.contains("dontWelcome")){
            editor.putBoolean("dontWelcome", false);
            editor.commit();
        }
    }

    // 更新可视度
    private void refreshLvVisibility() {
        //决定应该展示notes还是plans
        boolean temp = sharedPreferences.getBoolean("content_switch", false);
        if(temp){
            lv_layout.setVisibility(GONE);
            lv_plan_layout.setVisibility(View.VISIBLE);
        }
        else{
            lv_layout.setVisibility(View.VISIBLE);
            lv_plan_layout.setVisibility(GONE);
        }
        if(temp) myToolbar.setTitle(R.string.all_plans);
        else myToolbar.setTitle(R.string.all_notes);
    }

    // 刷新ListView
    public void refreshListView() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int fabColor = sharedPreferences.getInt("fabColor", getResources().getColor(R.color.colorPrimary));
        fab.setBackgroundTintList(ColorStateList.valueOf(fabColor));

        // 新建Note的增删改查编辑器
        NoteCRUD Nop = new NoteCRUD(context);
        Nop.open();
        if (noteList.size() > 0) noteList.clear();
        noteList.addAll(Nop.getAllNotes());
        if (sharedPreferences.getBoolean("reverseSort", false)) sortNotes(noteList, 2);
        else sortNotes(noteList, 1);
        Nop.close();
        adapter.notifyDataSetChanged();

        // 新建Plan的增删改查编辑器
        PlanCRUD Pop = new PlanCRUD(context);
        Pop.open();
        if(planList.size() > 0) {
            cancelAlarms(planList);  // 删除所有提醒
            planList.clear();
        }
        planList.addAll(Pop.getAllPlans());

        if (!sharedPreferences.getBoolean("dontBother", false)) {
            startAlarms(planList);  // 添加所有提醒
        }

        if (sharedPreferences.getBoolean("reverseSort", false)) sortPlans(planList, 2);
        else sortPlans(planList, 1);
        Pop.close();
        planAdapter.notifyDataSetChanged();

        achievement.listen();
    }

    // 笔记排序
    public void sortNotes(List<Note> noteList, final int mode) {
        Collections.sort(noteList, new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                try {
                    if (mode == 1) {  // 按时间正序排列(新的在上面)
                        return npLong(dateStrToSec(o2.getTime()) - dateStrToSec(o1.getTime()));
                    }
                    else if (mode == 2) {  // 按时间倒序排列(新的在下面)
                        return npLong(dateStrToSec(o1.getTime()) - dateStrToSec(o2.getTime()));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 1;
            }
        });
    }

    // 提醒排序
    public void sortPlans(List<Plan> planList, final int mode){
        Collections.sort(planList, new Comparator<Plan>() {
            @Override
            public int compare(Plan o1, Plan o2) {
                try {
                    if (mode == 1)  // 按时间正序排列(新的在上面)
                        return npLong(calStrToSec(o2.getTime()) - calStrToSec(o1.getTime()));
                    else if (mode == 2)  // 按时间倒序排列(新的在下面)
                        return npLong(calStrToSec(o1.getTime()) - calStrToSec(o2.getTime()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 1;
            }
        });
    }

    // 将时间差转换为大于, 小于, 等于
    public int npLong(Long l) {
        if (l > 0) return 1;
        else if (l < 0) return -1;
        else return 0;
    }

    //格式转换 string -> milliseconds
    public long dateStrToSec(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long secTime = format.parse(date).getTime();
        return secTime;
    }

    // 删除所有提醒
    private void cancelAlarms(List<Plan> plans) {
        for(int i = 0; i < plans.size(); i++) cancelAlarm(plans.get(i));
    }

    // 删除一项提醒
    private void cancelAlarm(Plan p) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int)p.getId(), intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    // 添加所有提醒
    private void startAlarms(List<Plan> plans){
        for(int i = 0; i < plans.size(); i++) startAlarm(plans.get(i));
    }

    // 添加一项提醒
    private void startAlarm(Plan p) {
        Calendar c = p.getPlanTime();
        if(!c.before(Calendar.getInstance())) {
            Intent intent = new Intent(HomeActivity.this, AlarmReceiver.class);
            intent.putExtra("title", p.getTitle());
            intent.putExtra("content", p.getContent());
            intent.putExtra("id", (int)p.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) p.getId(), intent, 0);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        }
    }

    // 初始化左侧弹出窗口
    public void initPopupView() {
        layoutInflater = (LayoutInflater) HomeActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customView = (ViewGroup) layoutInflater.inflate(R.layout.side_layout, null);
        coverView = (ViewGroup) layoutInflater.inflate(R.layout.side_cover, null);

        main = findViewById(R.id.main_layout);

        // 实例化左侧弹出窗口
        wm = getWindowManager();
        metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
    }

    // 显示左侧弹出窗口
    private void showPopUpWindow() {
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        popupCover = new PopupWindow(coverView, width, height, false);
        popupWindow = new PopupWindow(customView, (int) (width * 0.7), (height), true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setAnimationStyle(R.style.AnimationFade);
        popupCover.setAnimationStyle(R.style.AnimationCover);

        // 显示左侧弹出窗口
        findViewById(R.id.main_layout).post(new Runnable() {  // 等Main Layout加载完再弹出
            @Override
            public void run() {
                popupCover.showAtLocation(main, Gravity.NO_GRAVITY, 0, 0);
                popupWindow.showAtLocation(main, Gravity.NO_GRAVITY, 0, 0);

                setting_card = customView.findViewById(R.id.setting_settings_card);
                show_all_card =customView.findViewById(R.id.show_all);
                lv_tag = customView.findViewById(R.id.lv_tag);
                add_tag = customView.findViewById(R.id.add_tag);

                add_tag.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (sharedPreferences.getString("tagListString","").split("_").length < MAX_TAG_NUM) {
                            final EditText et = new EditText(context);
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setMessage(R.string.enter_new_tag)
                                    .setView(et)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags

                                            String name = et.getText().toString();
                                            if (!tagList.contains(name)) {
                                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                                                String oldTagListString = sharedPreferences.getString("tagListString", null);
                                                String newTagListString = oldTagListString + "_" + name;
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("tagListString", newTagListString);
                                                editor.commit();
                                                refreshTagList();
                                            }
                                            else Toast.makeText(context, R.string.repeat_tag, Toast.LENGTH_SHORT).show();
                                        }
                                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                        }
                        else{
                            Toast.makeText(context, R.string.too_many_tag, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", "").split("_")); //获取tags
                tagAdapter = new TagAdapter(context, tagList, numOfTagNotes(tagList));
                lv_tag.setAdapter(tagAdapter);

                lv_tag.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if(!content_switch.isChecked()) {
                            List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", "").split("_")); //获取tags
                            int tag = position + 1;
                            List<Note> temp = new ArrayList<>();
                            for (int i = 0; i < noteList.size(); i++) {
                                if (noteList.get(i).getTag() == tag) {
                                    Note note = noteList.get(i);
                                    temp.add(note);
                                }
                            }
                            adapter = new NoteAdapter(context, temp);
//                            NoteAdapter tempAdapter = new NoteAdapter(context, temp);
                            lv.setAdapter(adapter);
                            myToolbar.setTitle(tagList.get(position));
//                            refreshListView();
                        }
                        popupWindow.dismiss();
                    }
                });

                lv_tag.setOnItemLongClickListener(new OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                        if (position > 4) {
                            resetTagsX(parent);
                            float length = getResources().getDimensionPixelSize(R.dimen.distance);
                            CardView circleCard = view.findViewById(R.id.blank_tag_card);
                            circleCard.animate().translationX(length).setDuration(300).start();
                            TextView blank = view.findViewById(R.id.blank_tag);
                            blank.animate().translationX(length).setDuration(300).start();
                            TextView text = view.findViewById(R.id.text_tag);
                            text.animate().translationX(length).setDuration(300).start();
                            ImageView del = view.findViewById(R.id.delete_tag);
                            del.setVisibility(View.VISIBLE);
                            del.animate().translationX(length).setDuration(300).start();

                            del.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new AlertDialog.Builder(HomeActivity.this)
                                            .setMessage(R.string.del_tag_hint)
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    int tag = position + 1;
                                                    for (int i = 0; i < noteList.size(); i++) {
                                                        // 设置被删除tag对应的Notes Tag = 1
                                                        Note temp = noteList.get(i);
                                                        if (temp.getTag() == tag) {
                                                            temp.setTag(1);
                                                            NoteCRUD Nop = new NoteCRUD(context);
                                                            Nop.open();
                                                            Nop.updateNote(temp);
                                                            Nop.close();
                                                        }
                                                    }
                                                    List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", "").split("_")); //获取tags
                                                    if(tag + 1 < tagList.size()) {
                                                        for (int j = tag + 1; j < tagList.size() + 1; j++) {
                                                            // 大于被删除的Tag的所有Tag索引减一
                                                            for (int i = 0; i < noteList.size(); i++) {
                                                                Note temp = noteList.get(i);
                                                                if (temp.getTag() == j) {
                                                                    temp.setTag(j - 1);
                                                                    NoteCRUD Nop = new NoteCRUD(context);
                                                                    Nop.open();
                                                                    Nop.updateNote(temp);
                                                                    Nop.close();
                                                                }
                                                            }
                                                        }
                                                    }

                                                    // 更新Preferences
                                                    List<String> newTagList = new ArrayList<>();
                                                    newTagList.addAll(tagList);
                                                    newTagList.remove(position);
                                                    String newTagListString = TextUtils.join("_", newTagList);
                                                    Log.d(TAG, "onClick: " + newTagListString);
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor.putString("tagListString", newTagListString);
                                                    editor.commit();

                                                    refreshTagList();
                                                }
                                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                                }
                            });
                            return true;
                        }
                        else {
                            Toast.makeText(getBaseContext(), "默认标签不能删除哦~", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });

                setting_card.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(new Intent(HomeActivity.this, SettingActivity.class), StaticUtils.REQUEST_SETTING);
                        overridePendingTransition(R.anim.in_lefttoright, R.anim.no);
                    }
                });

                show_all_card.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!content_switch.isChecked()) {
                            myToolbar.setTitle(R.string.all_notes);
                            adapter = new NoteAdapter(context, noteList);
                            lv.setAdapter(adapter);
                        }
                        popupWindow.dismiss();
                    }
                });

                coverView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        popupCover.dismiss();
                    }
                });
            }
        });
    }

    // 刷新Tag列表
    private void refreshTagList() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
        tagAdapter = new TagAdapter(context, tagList, numOfTagNotes(tagList));
        lv_tag.setAdapter(tagAdapter);
        tagAdapter.notifyDataSetChanged();
    }

    // 计算Tag对应的Note数量
    public List<Integer> numOfTagNotes(List<String> noteStringList){
        Integer[] numbers = new Integer[noteStringList.size()];
        for(int i = 0; i < numbers.length; i++) numbers[i] = 0;
        for(int i = 0; i < noteList.size(); i++){
            numbers[noteList.get(i).getTag() - 1] ++;
        }
        return Arrays.asList(numbers);
    }

    // 长按一个Tag，将其他Tag归位
    private void resetTagsX(AdapterView<?> parent) {
        for (int i = 0; i < parent.getCount(); i++) {
            View view = parent.getChildAt(i);
            if (view != null) {
                TextView tvtv = view.findViewById(R.id.text_tag);
                Log.d("pos",i + ":" + tvtv.getText());
                if (view.findViewById(R.id.delete_tag).getVisibility() == View.VISIBLE) {
                    float length = 0;
                    CardView circleCard = view.findViewById(R.id.blank_tag_card);
                    circleCard.animate().translationX(length).setDuration(300).start();
                    TextView blank = view.findViewById(R.id.blank_tag);
                    blank.animate().translationX(length).setDuration(300).start();
                    TextView text = view.findViewById(R.id.text_tag);
                    text.animate().translationX(length).setDuration(300).start();
                    ImageView del = view.findViewById(R.id.delete_tag);
                    del.setVisibility(GONE);
                    del.animate().translationX(length).setDuration(300).start();
                }
            }
        }
    }

    // 创建右侧菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

//        mCalender = menu.findItem(R.id.calender);
//
//        if (sharedPreferences.getBoolean("content_switch", false)) {
//            mCalender.setVisible(true);
//        }
//        else {
//            mCalender.setVisible(false);
//        }

        // 旧搜索的代码
//        MenuItem mSearch = menu.findItem(R.id.action_search);
//        SearchView mSearchView = (SearchView) mSearch.getActionView();
//
//        TextView textView = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
//        textView.setTextColor(Color.WHITE);  // 字体颜色
//        textView.setHintTextColor(getResources().getColor(R.color.greyC));  // 提示颜色
//
//        ImageView closeImg = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
//        closeImg.setImageResource(R.drawable.ic_baseline_close_24);
//
//        mSearchView.setQueryHint(getResources().getString(R.string.serach_hint));
//        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                if(content_switch.isChecked()) planAdapter.getFilter().filter(newText);
//                else adapter.getFilter().filter(newText);
//                return false;
//            }
//        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                final View view = findViewById(R.id.menu_clear);
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setTitle(R.string.delete_during);
                        builder.setIcon(R.drawable.ic_error_outline_prim_24dp);
                        builder.setItems(list_String, new DialogInterface.OnClickListener() { // 列表对话框
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
                                int mode = (content_switch.isChecked()? 2 : 1);
                                String itemName = (mode == 1 ? "笔记" : "提醒");
                                new AlertDialog.Builder(HomeActivity.this)
                                        .setMessage("确认删除" + list_String[which] + "的" + itemName + "吗?")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int a) {
                                                Log.d(TAG, "onClick: " + which);
                                                removeSelectItems(which, mode);
                                                refreshListView();
                                            }

                                            // 删除对应笔记/提醒
                                            private void removeSelectItems(int which, int mode) {
                                                int monthNum = 0;
                                                switch (which){
                                                    case 0:
                                                        monthNum = 1;
                                                        break;
                                                    case 1:
                                                        monthNum = 3;
                                                        break;
                                                    case 2:
                                                        monthNum = 6;
                                                        break;
                                                    case 3:
                                                        monthNum = 12;
                                                        break;
                                                }
                                                Calendar rightNow = Calendar.getInstance();
                                                rightNow.add(Calendar.MONTH,-monthNum);  // 日期向后推
                                                Date selectDate = rightNow.getTime();
                                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                String selectDateStr = simpleDateFormat.format(selectDate);
                                                Log.d(TAG, "removeSelectItems: " + selectDateStr);
                                                switch(mode){
                                                    case 1: //Notes
                                                        dbHelper = new NoteDatabase(context);
                                                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                                                        Cursor cursor = db.rawQuery("select * from notes" ,null);
                                                        while(cursor.moveToNext()){
                                                            if (cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)).compareTo(selectDateStr) < 0){
                                                                db.delete("notes", NoteDatabase.ID + "=?", new String[]{Long.toString(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)))});
                                                            }
                                                        }
                                                        db.execSQL("update sqlite_sequence set seq=0 where name='notes'"); //reset id to 1
                                                        refreshListView();
                                                        break;
                                                    case 2: //Plans
                                                        planDbHelper = new PlanDatabase(context);
                                                        SQLiteDatabase pdb = planDbHelper.getWritableDatabase();
                                                        Cursor pcursor = pdb.rawQuery("select * from plans" ,null);
                                                        while(pcursor.moveToNext()){
                                                            if (pcursor.getString(pcursor.getColumnIndex(PlanDatabase.TIME)).compareTo(selectDateStr) < 0){
                                                                pdb.delete("plans", PlanDatabase.ID + "=?", new String[]{Long.toString(pcursor.getLong(pcursor.getColumnIndex(PlanDatabase.ID)))});
                                                            }
                                                        }
                                                        pdb.execSQL("update sqlite_sequence set seq=0 where name='plans'");
                                                        refreshListView();
                                                        break;
                                                }
                                            }
                                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create().show();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return true;
                    }
                });
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    // 菜单项目点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                if(!content_switch.isChecked()) {
                    new AlertDialog.Builder(HomeActivity.this)
                            .setMessage(R.string.delete_all_notes_q)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dbHelper = new NoteDatabase(context);
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    db.delete("notes", null, null);//delete data in table NOTES
                                    db.execSQL("update sqlite_sequence set seq=0 where name='notes'"); //reset id to 1
                                    refreshListView();
                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }
                else{
                    new AlertDialog.Builder(HomeActivity.this)
                            .setMessage(R.string.delete_all_plans_q)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    planDbHelper = new PlanDatabase(context);
                                    SQLiteDatabase db = planDbHelper.getWritableDatabase();
                                    db.delete("plans", null, null);//delete data in table NOTES
                                    db.execSQL("update sqlite_sequence set seq=0 where name='plans'"); //reset id to 1
                                    refreshListView();
                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }
                break;
//            case R.id.calender:
                // todo 打开CalenderActivity
//                mCalender.setVisible(false);
//                content_switch.setVisibility(GONE);
//                mSearchView.autoOpenOrClose();
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 点击事件（listView）
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.lv:
                Note curNote = (Note) parent.getItemAtPosition(position);
                Intent intent = new Intent(HomeActivity.this, EditActivity.class);
                intent.putExtra("content", curNote.getContent());
                intent.putExtra("id", curNote.getId());
                intent.putExtra("time", curNote.getTime());
                intent.putExtra("loc", curNote.getLoc());
                intent.putExtra("mode", 3);  // 编辑已存在的Note
                intent.putExtra("tag", curNote.getTag());
                startActivityForResult(intent, StaticUtils.REQUEST_EDIT);
                overridePendingTransition(R.anim.in_righttoleft, R.anim.out_righttoleft);
                break;
            case R.id.lv_plan:
                Plan curPlan = (Plan) parent.getItemAtPosition(position);
                Intent intent1 = new Intent(HomeActivity.this, EditAlarmActivity.class);
                intent1.putExtra("title", curPlan.getTitle());
                intent1.putExtra("content", curPlan.getContent());
                intent1.putExtra("time", curPlan.getTime());
                intent1.putExtra("mode", 1);  // 编辑已存在的Plan
                intent1.putExtra("id", curPlan.getId());
                startActivityForResult(intent1, StaticUtils.REQUEST_EDIT);
                overridePendingTransition(R.anim.in_righttoleft, R.anim.out_righttoleft);
                break;
        }
    }

    // 长按事件（listView）
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.lv:
                final Note note = noteList.get(position);
                new AlertDialog.Builder(HomeActivity.this)
                        .setMessage(R.string.delete_one_note_q)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NoteCRUD op = new NoteCRUD(context);
                                op.open();
                                op.removeNote(note);
                                op.close();
                                refreshListView();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
            case R.id.lv_plan:
                final Plan plan = planList.get(position);
                new AlertDialog.Builder(HomeActivity.this)
                        .setMessage(R.string.delete_one_plan_q)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PlanCRUD op = new PlanCRUD(context);
                                op.open();
                                op.removePlan(plan);
                                op.close();
                                refreshListView();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == StaticUtils.REQUEST_EDIT) {
            int returnMode;
            long note_Id;
            if (data != null) {
                returnMode = data.getExtras().getInt("mode", -1);
                note_Id = data.getExtras().getLong("id", 0);
                if (returnMode == 1) {   // 修改了Note
                    String content = data.getExtras().getString("content");
                    String time = data.getExtras().getString("time");
                    String loc = data.getExtras().getString("loc", "无位置信息");
                    int tag = data.getExtras().getInt("tag", 1);
                    Note newNote = new Note(content, time, loc, tag);
                    newNote.setId(note_Id);
                    NoteCRUD op = new NoteCRUD(context);
                    op.open();
                    op.updateNote(newNote);
                    achievement.editNote(op.getNote(note_Id).getContent(), content);
                    op.close();
                } else if (returnMode == 2) {   // 删除Note
                    Note curNote = new Note();
                    curNote.setId(note_Id);
                    NoteCRUD op = new NoteCRUD(context);
                    op.open();
                    op.removeNote(curNote);
                    op.close();
                    achievement.deleteNote();
                } else if (returnMode == 0) {   // 新建Note
                    String content = data.getExtras().getString("content");
                    String time = data.getExtras().getString("time");
                    String loc = data.getExtras().getString("loc", "无位置信息");
                    int tag = data.getExtras().getInt("tag", 1);
                    Note newNote = new Note(content, time, loc, tag);
                    NoteCRUD op = new NoteCRUD(context);
                    op.open();
                    op.addNote(newNote);
                    op.close();
                    achievement.addNote(content);
                } else if (returnMode == 11) {   // 修改了Plan
                    String title = data.getExtras().getString("title", null);
                    String content = data.getExtras().getString("content", null);
                    String time = data.getExtras().getString("time", null);
                    Log.d(TAG, time);
                    Plan plan = new Plan(title, content, time);
                    plan.setId(note_Id);
                    PlanCRUD op = new PlanCRUD(context);
                    op.open();
                    op.updatePlan(plan);
                    op.close();
                } else if (returnMode == 12) {   // 删除Plan
                    Plan plan = new Plan();
                    plan.setId(note_Id);
                    PlanCRUD op = new PlanCRUD(context);
                    op.open();
                    op.removePlan(plan);
                    op.close();
                } else if (returnMode == 10) {   // 新建Plan
                    String title = data.getExtras().getString("title", null);
                    String content = data.getExtras().getString("content", null);
                    String time = data.getExtras().getString("time", null);
                    Plan newPlan = new Plan(title, content, time);
                    PlanCRUD op = new PlanCRUD(context);
                    op.open();
                    op.addPlan(newPlan);
                    op.close();
                } else {
                }
                refreshListView();
            }
        } else if (requestCode == StaticUtils.REQUEST_SETTING) {
             if (data.getExtras().getBoolean("need_fresh", true)) {
                 refreshListView();
             }
             if (data.getExtras().getBoolean("need_cancel", false)) {
                 // 删除闹钟
                 refreshListView();
             }
        }
    }

    // 成就系统
    public class Achievement {
        private SharedPreferences sharedPreferences;

        private int noteNumber;
        private int wordNumber;

        private int noteLevel;
        private int wordLevel;

        public Achievement(Context context) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            initPref();
            getPref();
        }

        private void getPref() {
            noteNumber = sharedPreferences.getInt("noteNumber", 0);
            wordNumber = sharedPreferences.getInt("wordNumber", 0);
            noteLevel = sharedPreferences.getInt("noteLevel", 0);
            wordLevel = sharedPreferences.getInt("wordLevel", 0);
        }

        private void initPref() {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (!sharedPreferences.contains("noteLevel")) {
                editor.putInt("noteLevel", 0);
                editor.commit();
                if (!sharedPreferences.contains("wordLevel")) {
                    editor.putInt("wordLevel", 0);
                    editor.commit();

                    addCurrent(noteList);
                    if (sharedPreferences.contains("maxRemainNumber")) {
                        editor.remove("maxRemainNumber");
                        editor.commit();
                    }
                    if (sharedPreferences.contains("remainNumber")){
                        editor.remove("remainNumber");
                        editor.commit();
                    }
                    if (!sharedPreferences.contains("noteNumber")) {
                        editor.putInt("noteNumber", 0);
                        editor.commit();
                        addCurrent(noteList);
                        if (!sharedPreferences.contains("wordNumber")) {
                            editor.putInt("wordNumber", 0);
                            editor.commit();

                        }
                    }

                }
            }
        }

        // 加入已写好的笔记
        private void addCurrent(List<Note> list) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            int tempNN = list.size();
            editor.putInt("noteNumber", tempNN);
            if (tempNN >= 1000) editor.putInt("noteLevel", 4);
            else if (tempNN >= 100) editor.putInt("noteLevel", 3);
            else if (tempNN >= 10) editor.putInt("noteLevel", 2);
            else if (tempNN >= 1) editor.putInt("noteLevel", 1);
            int wordCount = 0;
            for (int i = 0; i < list.size(); i++) {
                wordCount += list.get(i).getContent().length();
            }
            editor.putInt("wordNumber", wordCount);
            if (wordCount >= 20000) editor.putInt("noteLevel", 5);
            else if (wordCount >= 5000) editor.putInt("noteLevel", 4);
            else if (wordCount >= 1000) editor.putInt("noteLevel", 3);
            else if (wordCount >= 500) editor.putInt("noteLevel", 2);
            else if (wordCount >= 100) editor.putInt("noteLevel", 1);
            editor.commit();
        }

        // 添加笔记
        public void addNote(String content) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            noteNumber++;
            editor.putInt("noteNumber", noteNumber);

            wordNumber += content.length();
            editor.putInt("wordNumber", wordNumber);

            editor.commit();
        }

        // 删除笔记
        public void deleteNote() {

        }

        // 编辑笔记，修改字数
        public void editNote(String oldContent, String newContent) {
            if (newContent.length() > oldContent.length()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                wordNumber += (newContent.length() - oldContent.length());
                editor.putInt("wordNumber", wordNumber);
                editor.commit();
            }
        }

        // 笔记数成就
        public void noteNumberAchievement(int num) {
            switch (num) {
                case 1:
                    if (noteLevel == 0) announcement(getResources().getString(R.string.your_first_step), 1, num);
                    break;
                case 10:
                    if (noteLevel == 1) announcement(getResources().getString(R.string.your_ten_step), 1, num);
                    break;
            }
        }

        // 字数成就
        public void wordNumberAchievement(int num) {
            if (num > 20000 && wordLevel == 4)
                announcement(getResources().getString(R.string.num_ache_sentence_4), 2, 20000);
            else if (num > 5000 && wordLevel == 3)
                announcement(getResources().getString(R.string.num_ache_sentence_3), 2, 5000);
            else if (num > 1000 && wordLevel == 2)
                announcement(getResources().getString(R.string.num_ache_sentence_2), 2, 1000);
            else if (num > 500 && wordLevel == 1)
                announcement(getResources().getString(R.string.num_ache_sentence_1), 2, 500);
            else if (num > 100 && wordLevel == 0)
                announcement(getResources().getString(R.string.num_ache_sentence_0), 2, 100);
        }

        // 对话框
        public void announcement(String message, int mode, int num) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle(annoucementTitle(mode, num))
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
            setState(mode);
        }

        // 对话框标题
        public String annoucementTitle(int mode, int num) {
            switch (mode) {
                case 1:
                    return getResources().getString(R.string.you_have_write) + num + "笔记了!";
                case 2:
                    return getResources().getString(R.string.you_have_write) + num + "字了！";
                case 3:
                    return "你触发了隐藏成就，你猜猜是什么 (笑";
            }
            return null;
        }

        public void setState(int mode) {
            //set corresponding state to true in case repetition of annoucement
            SharedPreferences.Editor editor = sharedPreferences.edit();
            switch (mode) {
                case 1:
                    noteLevel ++;
                    editor.putInt("noteLevel", noteLevel);
                    editor.commit();
                    break;
                case 2:
                    wordLevel ++;
                    editor.putInt("wordLevel", wordLevel);
                    editor.commit();
                    break;
            }
        }

        // 监听
        public void listen() {
            noteNumberAchievement(noteNumber);
            wordNumberAchievement(wordNumber);
        }
    }
}

