package com.zappkit.zappid.lemeor.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetAPKsNewVersionOutput extends BaseOutput {
    @SerializedName("data")
    public ArrayList<String> apks;
}
