package com.zappkit.zappid;

public class PlayItemModel {
    private int FreqDb = 1;
    private double freq = 1.0d;
    private int idDB;
    private int id = 0;
    private boolean loop = false;
    private String par = "";
    private int par_id = -1;
    private int seqDbId;
    private int playStatus;
    private int currentDuration;

    public void setLoop(boolean state) {
        this.loop = state;
    }

    public void setId(int customId) {
        this.id = customId;
    }

    public void setFrequency(double frequency) {
        this.freq = frequency;
    }

    public void setParent(String parent) {
        this.par = parent;
    }

    public void setFreqDatabase(int db) {
        this.FreqDb = db;
    }

    public void setParentId(int id) {
        this.par_id = id;
    }

    public void setSequenceDatabaseId(int id) {
        this.seqDbId = id;
    }

    public boolean getLoop() {
        return this.loop;
    }

    public int getId() {
        return this.id;
    }

    public String getIdString() {
        return Integer.toString(this.id);
    }

    public double getFrequency() {
        return this.freq;
    }

    public String getFrequencyString() {
        return Double.toString(this.freq);
    }

    public String getParent() {
        return this.par;
    }

    public int getFreqDatabase() {
        return this.FreqDb;
    }

    public int getParentId() {
        return this.par_id;
    }

    public int getSequenceDatabaseId() {
        return this.seqDbId;
    }

    public int getPlayStatus() {
        return playStatus;
    }

    public void setPlayStatus(int playStatus) {
        this.playStatus = playStatus;
    }

    public int getCurrentDuration() {
        return currentDuration;
    }

    public void setCurrentDuration(int currentDuration) {
        this.currentDuration = currentDuration;
    }

    public int getIdDB() {
        return idDB;
    }

    public void setIdDB(int idDB) {
        this.idDB = idDB;
    }
}
