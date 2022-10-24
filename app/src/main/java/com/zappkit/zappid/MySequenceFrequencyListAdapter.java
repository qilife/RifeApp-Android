package com.zappkit.zappid;

import android.app.Activity;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MySequenceFrequencyListAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private Activity activity;
    LocalBroadcastManager broadcaster;
    private ArrayList data;
    FrequencyListModel tempValues = null;

    public MySequenceFrequencyListAdapter(Activity a, ArrayList d) {
        this.activity = a;
        this.data = d;
        inflater = (LayoutInflater) this.activity.getSystemService("layout_inflater");
        this.broadcaster = LocalBroadcastManager.getInstance(this.activity);
    }

    public int getCount() {
        return this.data.size();
    }

    public Object getItem(int position) {
        return Integer.valueOf(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        this.tempValues = null;
        this.tempValues = (FrequencyListModel) this.data.get(position);
//        vi = inflater.inflate(R.layout.mysequence_addedfreq_item, null);
        vi = inflater.inflate(R.layout.item_addfreq_my_program, null);
        final MySeqFreqViewHolder holder = new MySeqFreqViewHolder();
        holder.name = (TextView) vi.findViewById(R.id.mySeqFreqName);
        holder.position = position;
        holder.imgdelete = (ImageView) vi.findViewById(R.id.mySeqFreqDeletebtn);
        holder.values = this.tempValues;
        holder.name.setText(this.tempValues.getFrequencyString() + " Hz");
        holder.imgdelete.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("removeFreqFromSequence");
                intent.putExtra("position", holder.position);
                MySequenceFrequencyListAdapter.this.broadcaster.sendBroadcast(intent);
            }
        });
        return vi;
    }
}
