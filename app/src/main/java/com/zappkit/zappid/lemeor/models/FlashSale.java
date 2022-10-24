package com.zappkit.zappid.lemeor.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class FlashSale implements Serializable{
    @SerializedName("enable")
    boolean enable = false;
    @SerializedName("init_delay")
    float initDelay;
    @SerializedName("duration")
    float duration;
    @SerializedName("interval")
    float interval;
    @SerializedName("proposals_count")
    float proposalsCount;
    @SerializedName("ntf")
    Ntf ntf;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public float getInitDelay() {
        return initDelay;
    }

    public void setInitDelay(float initDelay) {
        this.initDelay = initDelay;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public float getInterval() {
        return interval;
    }

    public void setInterval(float interval) {
        this.interval = interval;
    }

    public float getProposalsCount() {
        return proposalsCount;
    }

    public void setProposalsCount(float proposalsCount) {
        this.proposalsCount = proposalsCount;
    }

    public Ntf getNtf() {
        return ntf;
    }

    public void setNtf(Ntf ntf) {
        this.ntf = ntf;
    }
}