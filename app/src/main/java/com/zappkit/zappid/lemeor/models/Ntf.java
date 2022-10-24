package com.zappkit.zappid.lemeor.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Ntf implements Serializable {
    @SerializedName("first")
    AlarmMessage first;
    @SerializedName("second")
    AlarmMessage second;
    @SerializedName("third")
    AlarmMessage third;

    public AlarmMessage getFirst() {
        return first;
    }

    public void setFirst(AlarmMessage first) {
        this.first = first;
    }

    public AlarmMessage getSecond() {
        return second;
    }

    public void setSecond(AlarmMessage second) {
        this.second = second;
    }

    public AlarmMessage getThird() {
        return third;
    }

    public void setThird(AlarmMessage third) {
        this.third = third;
    }
}