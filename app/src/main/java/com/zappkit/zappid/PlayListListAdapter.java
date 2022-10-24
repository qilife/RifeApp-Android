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

public class PlayListListAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private Activity activity;
    LocalBroadcastManager broadcaster;
    private ArrayList<?> data;
    PlayListListModel tempValues = null;

    public PlayListListAdapter(Activity a, ArrayList d) {
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
        this.tempValues = (PlayListListModel) this.data.get(position);
        vi = inflater.inflate(R.layout.item_my_program, null);
        final playlistViewHolder holder = new playlistViewHolder();
        holder.name = (TextView) vi.findViewById(R.id.mySeqName);
        holder.position = position;
        holder.delimg = (ImageView) vi.findViewById(R.id.mySeqDeletebtn);
        holder.editimg = (ImageView) vi.findViewById(R.id.mySeqEditBtn);
        holder.values = this.tempValues;
        holder.name.setText(this.tempValues.getName());
        holder.delimg.setOnClickListener(new OnClickListener() {

            class C01721 implements DialogInterface.OnClickListener {
                C01721() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(PlayListListAdapter.this.activity, PlayListListAdapter.this.activity.getResources().getString(R.string.myPlaylistDeleteDialogCompleted), 0).show();
                    Intent intent = new Intent("removePlaylist");
                    intent.putExtra("position", holder.position);
                    PlayListListAdapter.this.broadcaster.sendBroadcast(intent);
                }
            }

            class C01732 implements DialogInterface.OnClickListener {
                C01732() {
                }

                public void onClick(DialogInterface dialog, int which) {
                }
            }

            public void onClick(View v) {
                Builder builder = new Builder(PlayListListAdapter.this.activity);
                builder.setTitle(PlayListListAdapter.this.activity.getResources().getString(R.string.myFrequencyDeleteDialogTitle));
                builder.setMessage(PlayListListAdapter.this.activity.getResources().getString(R.string.myPlaylistDeleteDialogText));
                builder.setPositiveButton(PlayListListAdapter.this.activity.getResources().getString(R.string.myFrequencyDeleteDialogYes), new C01721());
                builder.setNegativeButton(PlayListListAdapter.this.activity.getResources().getString(R.string.myFrequencyDeleteDialogNo), new C01732());
                builder.create().show();
            }
        });
        holder.editimg.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("editPlaylist");
                intent.putExtra("position", holder.position);
                PlayListListAdapter.this.broadcaster.sendBroadcast(intent);
            }
        });
        return vi;
    }

    static class playlistViewHolder {
        ImageView delimg;
        ImageView editimg;
        TextView name;
        int position;
        PlayListListModel values;

        playlistViewHolder() {
        }
    }
}
