package com.zappkit.zappid.lemeor.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Advertisements implements Serializable {

    @SerializedName("enable")
    boolean enable;
    @SerializedName("enable_banner")
    boolean enableBanner;

    public boolean isEnable() {
        return enable;
    }

    public boolean isEnableBanner() {
        return enableBanner;
    }
}
