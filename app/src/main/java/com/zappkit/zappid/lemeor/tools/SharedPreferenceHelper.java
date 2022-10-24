package com.zappkit.zappid.lemeor.tools;

import android.content.Context;
import android.content.SharedPreferences;

import com.zappkit.zappid.BuildConfig;

public class SharedPreferenceHelper {
    public static final String SHARED_PREF_NAME = BuildConfig.PREF_TITLE;
    private static SharedPreferenceHelper mInstance;
    private SharedPreferences mSharedPreferences;

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    private SharedPreferenceHelper(Context context) {
        mSharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPreferenceHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPreferenceHelper(context);
        }
        return mInstance;
    }

    public void set(String key, String value) {
        mSharedPreferences.edit().putString(key, value).apply();
    }

    public String get(String key) {
        return mSharedPreferences.getString(key, null);
    }

    public void setInt(String key, int value) {
        mSharedPreferences.edit().putInt(key, value).apply();
    }

    public int getInt(String key) {
        return mSharedPreferences.getInt(key, 0);
    }

    public void setBool(String key, boolean value) {
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBool(String key) {
        if (BuildConfig.IS_FREE) {
            if (key != null && key.equalsIgnoreCase(Constants.KEY_PURCHASED)){
                return true;
            }
        }
        return mSharedPreferences.getBoolean(key, false);
    }

    public void setLong(String key, long value) {
        mSharedPreferences.edit().putLong(key, value).apply();
    }

    public long getLong(String key) {
        return mSharedPreferences.getLong(key, 0);
    }
    public void setPriceByCurrency(String priceKey, String value) {
        mSharedPreferences.edit().putString(priceKey, value).apply();
    }

    public String getPriceByCurrency(String priceKey) {
        String price = mSharedPreferences.getString(priceKey, null);
        if(price == null){
            if(priceKey.equalsIgnoreCase(Constants.PRICE_7_DAY_TRIAL)){
                return "$24.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_1_MONTH)){
                return "$24.99";
            }else if (priceKey.equalsIgnoreCase(Constants.PRICE_1_YEAR)){
                return "$199.99";
            }
        }
        return price;
    }
}
