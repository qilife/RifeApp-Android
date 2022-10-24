package com.zappkit.zappid;

public class FrequencyListModel {
    private int databaseId = 1;
    private double frequency = 1.0d;
    private int id = 0;

    public void setFrequency(double Frequency) {
        this.frequency = Frequency;
    }

    public void setDatabaseId(int dbId) {
        this.databaseId = dbId;
    }

    public void setId(int customId) {
        this.id = customId;
    }

    public double getFrequency() {
        return this.frequency;
    }

    public String getFrequencyString() {
        return Double.toString(this.frequency);
    }

    public int getId() {
        return this.id;
    }

    public String getIdString() {
        return Integer.toString(this.id);
    }

    public int getDatabaseId() {
        return this.databaseId;
    }
}
