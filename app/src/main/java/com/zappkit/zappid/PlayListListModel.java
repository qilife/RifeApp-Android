package com.zappkit.zappid;

import com.zappkit.zappid.lemeor.models.SequenceListModel;

import java.util.ArrayList;

public class PlayListListModel {
    private int id = -1;
    private String list = "";
    private String name = "";
    private String notes = "";
    private ArrayList<SequenceListModel> sequenceListModels;

    public void setName(String name) {
        this.name = name;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setId(int customId) {
        this.id = customId;
    }

    public void setList(String list) {
        this.list = list;
    }

    public String getName() {
        return this.name;
    }

    public String getNotes() {
        return this.notes;
    }

    public int getId() {
        return this.id;
    }

    public String getList() {
        return this.list;
    }

    public ArrayList<String> getArrayList() {
        ArrayList<String> output = new ArrayList();
        for (String val : this.list.split("-")) {
            output.add(val);
        }
        return output;
    }

    public ArrayList<SequenceListModel> getSequenceListModels() {
        return sequenceListModels;
    }

    public void setSequenceListModels(ArrayList<SequenceListModel> sequenceListModels) {
        this.sequenceListModels = sequenceListModels;
    }
}
