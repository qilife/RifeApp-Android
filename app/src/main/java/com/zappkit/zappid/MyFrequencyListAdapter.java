package com.zappkit.zappid;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class MyFrequencyListAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private Activity activity;
    LocalBroadcastManager broadcaster;
    private ArrayList data;
    FrequencyListModel tempValues = null;

    public MyFrequencyListAdapter(Activity a, ArrayList d) {
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
        final MyFreqListViewHolder holder = new MyFreqListViewHolder();
        View vi = convertView;
        this.tempValues = null;
        this.tempValues = (FrequencyListModel) this.data.get(position);
        holder.values = this.tempValues;
//        vi = inflater.inflate(R.layout.mysequence_addedfreq_item, null);
        vi = inflater.inflate(R.layout.item_addedfreg_my_frequencies, null);
        holder.position = position;
        holder.frequency = (TextView) vi.findViewById(R.id.mySeqFreqName);
        holder.frequency.setText(this.tempValues.getFrequencyString() + " Hz");
        holder.imgdelete = (ImageView) vi.findViewById(R.id.mySeqFreqDeletebtn);
        holder.imgdelete.setOnClickListener(new OnClickListener() {

            class C01491 implements DialogInterface.OnClickListener {
                C01491() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MyFrequencyListAdapter.this.activity, MyFrequencyListAdapter.this.activity.getResources().getString(R.string.myFrequencyDeleteDialogCompleted), 0).show();
                    Intent intent = new Intent("removeFreq");
                    intent.putExtra("position", holder.position);
                    MyFrequencyListAdapter.this.broadcaster.sendBroadcast(intent);
                }
            }

            class C01502 implements DialogInterface.OnClickListener {
                C01502() {
                }

                public void onClick(DialogInterface dialog, int which) {
                }
            }

            public void onClick(View v) {
                Builder builder = new Builder(MyFrequencyListAdapter.this.activity);
                builder.setTitle(MyFrequencyListAdapter.this.activity.getResources().getString(R.string.myFrequencyDeleteDialogTitle));
                builder.setMessage(MyFrequencyListAdapter.this.activity.getResources().getString(R.string.myFrequencyDeleteDialogText));
                builder.setPositiveButton(MyFrequencyListAdapter.this.activity.getResources().getString(R.string.myFrequencyDeleteDialogYes), new C01491());
                builder.setNegativeButton(MyFrequencyListAdapter.this.activity.getResources().getString(R.string.myFrequencyDeleteDialogNo), new C01502());
                builder.create().show();
            }
        });
        return vi;
    }

    static class MyFreqListViewHolder {
        public TextView frequency;
        public ImageView imgdelete;
        public TextView name;
        public int position;
        public FrequencyListModel values;

        MyFreqListViewHolder() { }
    }
}
