package com.tsukiyoumi.myqrnote.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tsukiyoumi.myqrnote.db.PlanDatabase;

import java.util.ArrayList;
import java.util.List;

public class PlanCRUD {
    SQLiteOpenHelper dbHandler;
    SQLiteDatabase db;

    private static final String[] columns = {
            PlanDatabase.ID,
            PlanDatabase.TITLE,
            PlanDatabase.CONTENT,
            PlanDatabase.TIME,
    };

    public PlanCRUD(Context context){
        dbHandler = new PlanDatabase(context);
    }

    public void open(){
        db = dbHandler.getWritableDatabase();
    }

    public void close(){
        dbHandler.close();
    }

    public Plan addPlan(Plan plan){
        // 增
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlanDatabase.TITLE, plan.getTitle());
        contentValues.put(PlanDatabase.CONTENT, plan.getContent());
        contentValues.put(PlanDatabase.TIME, plan.getTime());
        long insertId = db.insert(PlanDatabase.TABLE_NAME, null, contentValues);
        plan.setId(insertId);

        return plan;
    }

    public Plan getPlan(long id){
        // 查
        Cursor cursor = db.query(PlanDatabase.TABLE_NAME,columns,PlanDatabase.ID + "=?",
                new String[]{String.valueOf(id)},null,null, null, null);
        if (cursor != null) cursor.moveToFirst();
        Plan e = new Plan(cursor.getString(1),cursor.getString(2), cursor.getString(3));

        return e;
    }

    public List<Plan> getAllPlans(){
        // 查(全部)
        Cursor cursor = db.query(PlanDatabase.TABLE_NAME,columns,null,null,null, null, null);

        List<Plan> plans = new ArrayList<>();
        if(cursor.getCount() > 0){
            while(cursor.moveToNext()){
                Plan plan = new Plan();
                plan.setId(cursor.getLong(cursor.getColumnIndex(PlanDatabase.ID)));
                plan.setTitle(cursor.getString(cursor.getColumnIndex(PlanDatabase.TITLE)));
                plan.setContent(cursor.getString(cursor.getColumnIndex(PlanDatabase.CONTENT)));
                plan.setTime(cursor.getString(cursor.getColumnIndex(PlanDatabase.TIME)));
                plans.add(plan);
            }
        }

        return plans;
    }

    public int updatePlan(Plan plan) {
        // 改
        ContentValues values = new ContentValues();
        values.put(PlanDatabase.TITLE, plan.getTitle());
        values.put(PlanDatabase.CONTENT, plan.getContent());
        values.put(PlanDatabase.TIME, plan.getTime());

        return db.update(PlanDatabase.TABLE_NAME, values,
                PlanDatabase.ID + "=?",new String[] { String.valueOf(plan.getId())});
    }

    public void removePlan(Plan plan) {
        // 删
        db.delete(PlanDatabase.TABLE_NAME, PlanDatabase.ID + "=" + plan.getId(), null);
    }
}
