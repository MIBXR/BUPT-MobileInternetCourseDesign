package com.tsukiyoumi.myqrnote.http;

import org.json.JSONException;

/**
 * API调结果用监听接口
 */
public interface ApiListener {

    void success(Api api) throws JSONException;

    void failure(Api api);
}
