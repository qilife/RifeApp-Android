package com.zappkit.zappid.lemeor.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Reminder implements Serializable {
    @SerializedName("launch_time")
    String launchTime;
    @SerializedName("interval")
    Double interval;
    @SerializedName("messages")
    ArrayList<String> messages;

    public String getLaunchTime() {
        return launchTime;
    }

    public void setLaunchTime(String launchTime) {
        this.launchTime = launchTime;
    }

    public Double getInterval() {
        return interval;
    }

    public void setInterval(Double interval) {
        this.interval = interval;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<String> messages) {
        this.messages = messages;
    }
}