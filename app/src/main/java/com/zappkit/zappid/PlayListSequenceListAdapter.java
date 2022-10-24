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

import com.zappkit.zappid.lemeor.models.SequenceListModel;

import java.util.ArrayList;

public class PlayListSequenceListAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private Activity activity;
    LocalBroadcastManager broadcaster;
    private ArrayList data;
    SequenceListModel tempValues = null;

    public PlayListSequenceListAdapter(Activity a, ArrayList d) {
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
//        vi = inflater.inflate(R.layout.mysequence_addedfreq_item, null);
        vi = inflater.inflate(R.layout.item_addfreq_my_program, null);
        final playlistSeqListViewHolder holder = new playlistSeqListViewHolder();
        holder.name = (TextView) vi.findViewById(R.id.mySeqFreqName);
        holder.position = position;
        holder.deleteBtn = (ImageView) vi.findViewById(R.id.mySeqFreqDeletebtn);
        holder.values = this.tempValues;
        holder.name.setText(this.tempValues.getSequenceTitle());
        holder.deleteBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("removeSeqFromPlaylist");
                intent.putExtra("position", holder.position);
                PlayListSequenceListAdapter.this.broadcaster.sendBroadcast(intent);
            }
        });
        return vi;
    }

    static class playlistSeqListViewHolder {
        ImageView deleteBtn;
        TextView name;
        int position;
        SequenceListModel values;

        playlistSeqListViewHolder() {
        }
    }
}
