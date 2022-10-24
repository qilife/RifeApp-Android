package com.zappkit.zappid.lemeor.main_menu.fragments.programs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.models.SequenceListModel;

import java.util.ArrayList;

public class ProgramsAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private ArrayList data;

    public ProgramsAdapter(Activity activity, ArrayList list) {
        this.data = list;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return this.data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        SequenceListModel tempValues = (SequenceListModel) this.data.get(position);

        if (tempValues.getNotes().equals("")) {
            view = inflater.inflate(R.layout.list_item_cp, null);
            ((TextView) view.findViewById(R.id.label)).setText(tempValues.getSequenceTitle());
            return view;
        }

        view = inflater.inflate(R.layout.item_program, null);
        TextView notes = view.findViewById(R.id.notes);
        ((TextView) view.findViewById(R.id.label)).setText(tempValues.getSequenceTitle());
        notes.setText(tempValues.getNotes());
        return view;
    }
}
