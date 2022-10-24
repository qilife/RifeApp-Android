package com.zappkit.zappid;

import android.app.Activity;
import android.content.SharedPreferences;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.lemeor.main_menu.player.FrequencyUIActivity;
import com.zappkit.zappid.lemeor.tools.Utilities;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;

import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zappkit.zappid.lemeor.base.BaseActivity;
import com.zappkit.zappid.lemeor.tools.Constants;

import java.util.ArrayList;

public class PlayItemAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private Activity activity;
    private ArrayList<PlayItemModel> data;
    DbHelper database;
    PlayItemModel tempValues = null;
    SharedPreferences settings;
    private int mCurrentPlayItemPosition = -1;
    private boolean isPausePlayer;

    public enum OPEN_LIST_TYPE {OPEN_2_SONGS, OPEN_ALL, OPEN_NONE}

    private OPEN_LIST_TYPE mOpenListType = OPEN_LIST_TYPE.OPEN_2_SONGS;
    private IOnClickListener mIOnClickListener;

    public PlayItemAdapter(Activity a, ArrayList<PlayItemModel> d) {
        this.activity = a;
        this.data = d;
        inflater = (LayoutInflater) this.activity.getSystemService("layout_inflater");
        settings = SharedPreferenceHelper.getSharedPreferences(activity);

        boolean purchased = SharedPreferenceHelper.getInstance(activity).getBool(Constants.KEY_PURCHASED);
        if(purchased){
            mOpenListType = OPEN_LIST_TYPE.OPEN_ALL;
        } else {
            mOpenListType = OPEN_LIST_TYPE.OPEN_2_SONGS;
        }
    }

    public void setOpenListType(OPEN_LIST_TYPE type){
        mOpenListType = type;
        notifyDataSetChanged();
    }

    public void setCurrentPlayItem(int position) {
        mCurrentPlayItemPosition = position;
    }

    public int getCurrentPlayItemPosition(){
        return mCurrentPlayItemPosition;
    }

    public void setPausePlayer(boolean isPause) {
        isPausePlayer = isPause;
        notifyDataSetChanged();
    }

    public boolean isPausePlayer(){
        return isPausePlayer;
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

    public void setIOnClickListener(IOnClickListener listener){
        mIOnClickListener = listener;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder = new ViewHolder();
        this.tempValues = null;
        this.tempValues = (PlayItemModel) this.data.get(position);
//        this.database = new DbHelper(this.activity);
//        vi = inflater.inflate(R.layout.play_item_layout, null);
        vi = inflater.inflate(R.layout.item_player, null);
        holder.values = this.tempValues;
        holder.name = (TextView) vi.findViewById(R.id.name);
        holder.tvTime = (TextView) vi.findViewById(R.id.tvTime);
//        holder.parent = (TextView) vi.findViewById(R.id.freqParent);
        // holder.imgloopbtn = (ImageView) vi.findViewById(R.id.loopbtn);
        holder.name.setText(holder.values.getFrequencyString() + " Hz");
//        holder.parent.setText(holder.values.getParent());

        holder.imagePlay = vi.findViewById(R.id.imv_play);

        holder.seekBar = vi.findViewById(R.id.sb_player);
//        holder.seekBar.getThumb().mutate().setAlpha(0);
        holder.seekBar.setPadding(0, 0, 0, 0);
        holder.seekBar.setMax(settings.getInt("FreqDuration", 180));
        holder.seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

//        holder.imagePlay.setSelected(false);
//        holder.imagePlay.setEnabled(true);
        holder.imagePlay.setImageResource(R.drawable.ic_play_white_small);
        if (mCurrentPlayItemPosition != -1 && mCurrentPlayItemPosition == position) {
            holder.seekBar.setVisibility(View.VISIBLE);
            holder.name.setVisibility(View.GONE);
            holder.seekBar.setProgress(this.tempValues.getCurrentDuration());
            holder.tvTime.setText(Utilities.timeCalc(this.tempValues.getCurrentDuration()));
            if (isPausePlayer) {
//                holder.imagePlay.setSelected(true);
                holder.imagePlay.setImageResource(R.drawable.ic_pause_small);
            } else {
//                holder.imagePlay.setEnabled(false);
                holder.imagePlay.setImageResource(R.drawable.ic_play_small);
            }
            holder.tvTime.setSelected(true);
        } else {
            holder.tvTime.setSelected(false);
            holder.seekBar.setVisibility(View.GONE);
            holder.name.setVisibility(View.VISIBLE);
//            if (holder.values.getPlayStatus() == 0) {
                holder.tvTime.setText(Utilities.timeCalc(settings.getInt("FreqDuration", 180)));
//            } else {
//                holder.tvTime.setText(Utilities.timeCalc(0));
//            }
        }

        holder.imagePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIOnClickListener != null){
                    if(mOpenListType == OPEN_LIST_TYPE.OPEN_2_SONGS && position >= 2){
                        if(activity instanceof FrequencyUIActivity){
                            ((BaseActivity)activity).showSubscriptionAppDialog();
                        }
                        return;
                    }
                    if(mCurrentPlayItemPosition != position && mCurrentPlayItemPosition != -1){
                        data.get(mCurrentPlayItemPosition).setCurrentDuration(0);
                        data.get(position).setCurrentDuration(0);
                    }
                    mIOnClickListener.onPlayPauseClick(position);
                }
            }
        });

        if (mOpenListType == OPEN_LIST_TYPE.OPEN_ALL) {
            holder.name.setTextColor(ContextCompat.getColor(activity, R.color.color_text));
            holder.tvTime.setTextColor(ContextCompat.getColor(activity, R.color.color_text));
        } else if (mOpenListType == OPEN_LIST_TYPE.OPEN_2_SONGS) {
            if(position < 2){
                holder.name.setTextColor(ContextCompat.getColor(activity, R.color.color_text));
                holder.tvTime.setTextColor(ContextCompat.getColor(activity, R.color.color_text));
            } else {
                holder.name.setTextColor(ContextCompat.getColor(activity, R.color.color_text_disable));
                holder.tvTime.setTextColor(ContextCompat.getColor(activity, R.color.color_text_disable));
                holder.seekBar.setVisibility(View.GONE);
                holder.name.setVisibility(View.VISIBLE);
            }
        } else if (mOpenListType == OPEN_LIST_TYPE.OPEN_NONE) {
            holder.name.setTextColor(ContextCompat.getColor(activity, R.color.color_text_disable));
            holder.tvTime.setTextColor(ContextCompat.getColor(activity, R.color.color_text_disable));
            holder.seekBar.setVisibility(View.GONE);
            holder.name.setVisibility(View.VISIBLE);
        }

//        holder.seekBar.setMax(3000);

//        if (holder.values.getLoop()) {
//            holder.imgloopbtn.setImageResource(R.drawable.loop);
//        } else {
//            holder.imgloopbtn.setImageResource(R.drawable.noloop);
//        }
//        if (!holder.values.getParent().equals("None")) {
//            holder.imgloopbtn.setOnLongClickListener(new OnLongClickListener() {
//                public boolean onLongClick(View v) {
//                    if (holder.values.getLoop()) {
//                        PlayItemAdapter.this.database.setSequenceLoop(false, holder.values.getParent(), holder.values.getParentId(), holder.values.getSequenceDatabaseId());
//                    } else {
//                        PlayItemAdapter.this.database.setSequenceLoop(true, holder.values.getParent(), holder.values.getParentId(), holder.values.getSequenceDatabaseId());
//                    }
//                    LocalBroadcastManager.getInstance(PlayItemAdapter.this.activity).sendBroadcast(new Intent("updateList"));
//                    if (!PlayItemAdapter.this.database.hasLoopedOptions()) {
//                        PlayItemAdapter.this.database.alterHasLoopedOptions();
//                    }
//                    return true;
//                }
//            });
//        }
//        holder.imgloopbtn.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                if (holder.values.getLoop()) {
//                    holder.values.setLoop(false);
//                    holder.imgloopbtn.setImageResource(R.drawable.noloop);
//                    PlayItemAdapter.this.database.setPlaylistFrequencyLoop(false, holder.values.getId(), holder.values.getParent());
//                } else {
//                    holder.values.setLoop(true);
//                    holder.imgloopbtn.setImageResource(R.drawable.loop);
//                    PlayItemAdapter.this.database.setPlaylistFrequencyLoop(true, holder.values.getId(), holder.values.getParent());
//                }
//                if (!PlayItemAdapter.this.database.hasLoopedOptions()) {
//                    Toast.makeText(PlayItemAdapter.this.activity, PlayItemAdapter.this.activity.getResources().getString(R.string.loopTriggerStr), 0).show();
//                }
//            }
//        });
        vi.setTag(holder);
        return vi;
    }

    public interface IOnClickListener{
        void onPlayPauseClick(int position);
    }
}
