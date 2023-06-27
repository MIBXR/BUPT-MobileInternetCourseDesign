package com.tsukiyoumi.myqrnote.baidu;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

//import com.tsukiyoumi.myqrnote.baidu.Base64Util;
//import com.tsukiyoumi.myqrnote.baidu.FileUtil;
//import com.tsukiyoumi.myqrnote.baidu.HttpUtil;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tsukiyoumi.myqrnote.entity.UserEntity;
import com.tsukiyoumi.myqrnote.http.Api;
import com.tsukiyoumi.myqrnote.http.ApiListener;
import com.tsukiyoumi.myqrnote.http.ApiUtil;
import com.tsukiyoumi.myqrnote.http.OkHttpUtil;
import com.tsukiyoumi.myqrnote.http.UniteApi;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import org.json.JSONObject;

/**
 * 通用文字识别
 */
public class OCRapi {
    public static String accessToken = "24.304aa1502d8ace5812a9854c6ef3f218.2592000.1625877187.282335-24344999";

    public OCRapi() {
        // 获取access_token
        String host = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=UPrTU1zr8iWPmCTkhGmsbhKC&client_secret=DcmCyclWDaOoApWqz2kuxkmuIpXisIGV";

        HashMap<String, String> hashMap = new HashMap<>();
        new UniteApi(host, hashMap).get(new ApiListener() {
            @Override
            public void success(Api api) throws JSONException {
                UniteApi uniteApi = (UniteApi) api;
                Gson gson = new Gson();
                accessToken = uniteApi.getJsonData().getString("access_token");
                Log.d("success", accessToken);
            }

            @Override
            public void failure(Api api) {
                Log.d("failure", "get accessToken fail");
            }
        });
    }

    public static String OCRapi(String filePath) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
        try {
            // 本地文件
            String imgStr = ImageSmaller.getSmallerImg(filePath);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            String param = "image=" + imgParam;

            String result = HttpUtil.post(url, accessToken, param);

            String result_join = "";
            JSONObject jo = new JSONObject(result);
            JSONArray words_result = jo.getJSONArray("words_result");
            for(int i = 0; i < words_result.length(); i++) {
                JSONObject item = words_result.getJSONObject(i);
                result_join = result_join + item.getString("words") + "\n";
            }
            return result_join;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


