package com.zappkit.zappid.lemeor;

import android.os.Build;
import android.os.StrictMode;
import android.text.TextUtils;

import androidx.multidex.MultiDexApplication;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;

import java.lang.reflect.Method;

public class RifeApp extends MultiDexApplication {

    public static final String TAG = RifeApp.class
            .getSimpleName();

    private RequestQueue mRequestQueue;

    private static RifeApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        FacebookSdk.sdkInitialize(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized RifeApp getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }


    public <T> void addToRequestQueue(JsonObjectRequest req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
