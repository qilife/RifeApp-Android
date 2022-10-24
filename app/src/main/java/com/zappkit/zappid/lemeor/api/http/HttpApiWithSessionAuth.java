package com.zappkit.zappid.lemeor.api.http;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.zappkit.zappid.lemeor.api.exception.ApiException;
import com.zappkit.zappid.lemeor.tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpApiWithSessionAuth extends AbstractHttpApi {
    private String mToken;
    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    public HttpApiWithSessionAuth(Context context) {
        super();
        mContext = context;
    }

    public void setCredentials(String token) {
        mToken = token;
    }

    public void clearCredentials() {
        mToken = null;
    }

    public boolean hasCredentials() {
        return !TextUtils.isEmpty(mToken);
    }

    private Map<String, String> createHeaderWithAuthorization() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Content-Type", "application/x-www-form-urlencoded");
        map.put("Accept", "application/json");
        if (hasCredentials()) {
            map.put("Authorization", mToken);
            return map;
        } else {
            return null;
        }
    }

    @Override
    public JSONObject doHttpPost(@NonNull String requestUrl, String jsonObject)
            throws ApiException, JSONException, IOException {
            JSONObject jsonResult = new JSONObject(executeHttpPost(requestUrl, createHeaderWithAuthorization(), jsonObject));
            try {
                if(jsonResult.getInt("ErrorCode") == Constants.FAILURE_SESSION_EXPIRED){
                    return new JSONObject(executeHttpPost(requestUrl, createHeaderWithAuthorization(), jsonObject));
                }
            } catch (JSONException ignored){ }
            return jsonResult;
    }

    @Override
    public JSONObject doHttpGet(@NonNull String requestUrl) throws ApiException, JSONException, IOException {
        JSONObject jsonResult = new JSONObject(executeHttpGet(requestUrl, createHeaderWithAuthorization()));
        try {
            if(jsonResult.getInt("ErrorCode") == Constants.FAILURE_SESSION_EXPIRED){
                return new JSONObject(executeHttpGet(requestUrl, createHeaderWithAuthorization()));
            }
        } catch (JSONException ignored){ }
        return jsonResult;
    }
}
