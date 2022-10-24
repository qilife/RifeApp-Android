package com.zappkit.zappid.lemeor.models;

import java.io.Serializable;

public class SequenceListModel implements Serializable {
    private String Notes = "";
    private String SequenceTitle = "";
    private int databaseId;
    private int id = -1;
    private String list;

    public void setSequenceTitle(String CompanyName) {
        this.SequenceTitle = CompanyName;
    }

    public void setNotes(String Image) {
        this.Notes = Image;
    }

    public void setId(int customId) {
        this.id = customId;
    }

    public void setDbId(int dbid) {
        this.databaseId = dbid;
    }

    public void setFrequecyList(String list) {
        this.list = list;
    }

    public String getSequenceTitle() {
        return this.SequenceTitle;
    }

    public String getNotes() {
        return this.Notes;
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

    public String getFrequencyList() {
        return this.list;
    }

    public String[] getFrequencyListArray() {
        return this.list.split("-");
    }
}
