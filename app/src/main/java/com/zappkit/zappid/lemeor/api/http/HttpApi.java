package com.zappkit.zappid.lemeor.api.http;

import androidx.annotation.NonNull;

import com.zappkit.zappid.lemeor.api.exception.ApiException;

import org.json.JSONException;

import java.io.IOException;

public interface HttpApi {

    Object doHttpPost(@NonNull String requestUrl, String jsonObject)
            throws JSONException, IOException, ApiException;

    Object doHttpGet(@NonNull String requestUrl)
            throws ApiException, JSONException, IOException;
}
