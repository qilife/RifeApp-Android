package com.zappkit.zappid.lemeor.api.models;

import com.google.gson.annotations.SerializedName;

public class BaseOutput {
    @SerializedName("code")
    public int code;
    @SerializedName("success")
    public boolean success;
}
