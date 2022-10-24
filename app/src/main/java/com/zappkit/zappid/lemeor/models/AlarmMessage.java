package com.zappkit.zappid.lemeor.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AlarmMessage implements Serializable{
    @SerializedName("message")
    String message;
    @SerializedName("delay")
    float delay;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public float getDelay() {
        return delay;
    }

    public void setDelay(float delay) {
        this.delay = delay;
    }
}