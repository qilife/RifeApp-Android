package com.zappkit.zappid.lemeor.api;

import android.content.Context;

import com.google.gson.Gson;
import com.zappkit.zappid.lemeor.api.exception.ApiException;
import com.zappkit.zappid.lemeor.api.models.GetAPKsNewVersionOutput;
import com.zappkit.zappid.lemeor.api.models.GetTokenOutput;
import com.zappkit.zappid.lemeor.api.http.HttpApiWithSessionAuth;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class TaskApi {
    public static final String TASK_WS = "http://www.quantum.ingeniusstudios.com/api";
    public static final String TASK_WS_PLASH_SALE = "https://irateadmin.ingeniusstudios.com/irateconfigs/v2/iOS_Rife_Machine_PEMF_Frequency.json";

    private HttpApiWithSessionAuth mHttpApi;
    private String mDomain;
    private Gson mGson;

    public TaskApi(Context context) {
        mHttpApi = new HttpApiWithSessionAuth(context);
        mGson = new Gson();
        mDomain = TASK_WS;
    }

    public void setCredentials(String token) {
        if (token == null || token.length() == 0)
            mHttpApi.clearCredentials();
        else
            mHttpApi.setCredentials(token);
    }

    public String getFullUrl(String subUrl) {
        return mDomain + subUrl;
    }

    public GetTokenOutput getToken() throws ApiException, JSONException, IOException {
        JSONObject requestData = new JSONObject();
        requestData.put("email", "hoanghuyhung@live.com");
        JSONObject data = mHttpApi.doHttpPost(getFullUrl("/token"), requestData.toString());
        return mGson.fromJson(data.toString(), GetTokenOutput.class);
    }

    public GetAPKsNewVersionOutput getAPKsNewVersion() throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpGet(getFullUrl("/getRiftAppAPKFiles"));
        return mGson.fromJson(data.toString(), GetAPKsNewVersionOutput.class);
    }
}
