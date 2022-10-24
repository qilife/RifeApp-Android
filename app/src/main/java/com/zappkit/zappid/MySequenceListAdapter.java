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

import com.zappkit.zappid.lemeor.models.SequenceListModel;

import java.util.ArrayList;

public class MySequenceListAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private Activity activity;
    LocalBroadcastManager broadcaster;
    private ArrayList data;
    SequenceListModel tempValues = null;

    public MySequenceListAdapter(Activity a, ArrayList d) {
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
        this.tempValues = (SequenceListModel) this.data.get(position);
//        vi = inflater.inflate(R.layout.mysequence_main_item, null);
        vi = inflater.inflate(R.layout.item_my_program, null);
        final mySequenceMainViewHolder holder = new mySequenceMainViewHolder();
        holder.position = position;
        holder.name = (TextView) vi.findViewById(R.id.mySeqName);
        holder.name.setText(this.tempValues.getSequenceTitle());
        holder.deleteImg = (ImageView) vi.findViewById(R.id.mySeqDeletebtn);
        holder.deleteImg.setOnClickListener(new OnClickListener() {

            class C01591 implements DialogInterface.OnClickListener {
                C01591() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MySequenceListAdapter.this.activity, MySequenceListAdapter.this.activity.getResources().getString(R.string.mySequenceDeleteDialogCompleted), 0).show();
                    Intent intent = new Intent("removeSeq");
                    intent.putExtra("position", holder.position);
                    MySequenceListAdapter.this.broadcaster.sendBroadcast(intent);
                }
            }

            class C01602 implements DialogInterface.OnClickListener {
                C01602() {
                }

                public void onClick(DialogInterface dialog, int which) {
                }
            }

            public void onClick(View v) {
                Builder builder = new Builder(MySequenceListAdapter.this.activity);
                builder.setTitle(MySequenceListAdapter.this.activity.getResources().getString(R.string.myFrequencyDeleteDialogTitle));
                builder.setMessage(MySequenceListAdapter.this.activity.getResources().getString(R.string.mySequenceDeleteDialogText));
                builder.setPositiveButton(MySequenceListAdapter.this.activity.getResources().getString(R.string.myFrequencyDeleteDialogYes), new C01591());
                builder.setNegativeButton(MySequenceListAdapter.this.activity.getResources().getString(R.string.myFrequencyDeleteDialogNo), new C01602());
                builder.create().show();
            }
        });
        holder.editImg = (ImageView) vi.findViewById(R.id.mySeqEditBtn);
        holder.editImg.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("editSequence");
                intent.putExtra("position", holder.position);
                MySequenceListAdapter.this.broadcaster.sendBroadcast(intent);
            }
        });
        return vi;
    }

    static class mySequenceMainViewHolder {
        ImageView deleteImg;
        ImageView editImg;
        TextView name;
        int position;
        SequenceListModel values;

        mySequenceMainViewHolder() {
        }
    }
}
