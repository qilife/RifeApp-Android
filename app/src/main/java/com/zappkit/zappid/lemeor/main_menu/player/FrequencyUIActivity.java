package com.zappkit.zappid.lemeor.main_menu.player;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;

import com.zappkit.zappid.PlayItemAdapter;
import com.zappkit.zappid.PlayItemModel;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.tools.WakeLocker;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zappkit.zappid.lemeor.base.BaseActivity;
import com.zappkit.zappid.lemeor.tools.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class FrequencyUIActivity extends BaseActivity {

    private BroadcastReceiver freqStatusReceiver;
    private BroadcastReceiver freqDataReceiver;
    private BroadcastReceiver pauseReceiver;
    private BroadcastReceiver updateListReceiver;
    private BroadcastReceiver timeReceiver;
    private BroadcastReceiver playPauseFirstReceiver;

    private SharedPreferences mSharedPreferences;
    private PlayItemAdapter mPlayItemAdapter;
    private Intent mPlayIntent;

    private int totalTimeBaseValue;

    private ArrayList<PlayItemModel> playItems = new ArrayList<>();
    private int mCurrentPlayItemPosition = -1;
    private int timeInt = 0;

    private TextView mTvContentTextPlayPause, mTvContentText, mTvFrequencies;
    private ImageView ivRepeatController;
    private ImageView ivPlayPause;

    @Override
    protected int initLayout() {
        return R.layout.activity_frequency_ui;
    }

    public void onCreate(Bundle savedInstanceState) {
        mSharedPreferences = SharedPreferenceHelper.getSharedPreferences(this);
        adjustLanguage();
        super.onCreate(savedInstanceState);

        setData();

        ivPlayPause = findViewById(R.id.pause_play_button);
        Cursor PlaylistItems = mDatabase.getPlayListItemsCursor();
        PlaylistItems.moveToFirst();

        if (PlaylistItems.getString(PlaylistItems.getColumnIndex("state")).equals("pause")) {
            ivPlayPause.setImageResource(R.drawable.ic_play);
            setPausePlayText(true);
            timeInt = PlaylistItems.getInt(PlaylistItems.getColumnIndex("time_lapsed"));
        } else {
            if (!isMyServiceRunning()) {
                mPlayIntent = new Intent(this, FrequencyService.class);
                startService(this.mPlayIntent);
            } else {
                ivPlayPause.setImageResource(R.drawable.ic_pause);
                setPausePlayText(false);
                timeInt = PlaylistItems.getInt(PlaylistItems.getColumnIndex("time_lapsed"));
            }
        }
        PlaylistItems.close();

        totalTimeBaseValue = (mDatabase.getPlaylistCountPlaying() - 1) * this.mSharedPreferences.getInt("FreqDuration", 180);
        final TextView totalText = findViewById(R.id.content_total_time);
        totalText.setText(timeCalc(totalTimeBaseValue + timeInt));

        freqStatusReceiver = new FreqStatusReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(freqStatusReceiver, new IntentFilter("Freq_status"));
        pauseReceiver = new PauseReceiver();
        registerReceiver(pauseReceiver, new IntentFilter("PauseFreq"));
        freqDataReceiver = new FreqDataReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(freqDataReceiver, new IntentFilter("FreqData"));
        timeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int time = intent.getIntExtra("current", 0);

                totalText.setText(FrequencyUIActivity.this.timeCalc(FrequencyUIActivity.this.totalTimeBaseValue + time));

                if (mCurrentPlayItemPosition < playItems.size() && mCurrentPlayItemPosition > -1) {
                    playItems.get(mCurrentPlayItemPosition).setCurrentDuration(mSharedPreferences.getInt("FreqDuration", 180) - time);
                    mPlayItemAdapter.setPausePlayer(true);
                    mPlayItemAdapter.notifyDataSetChanged();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(timeReceiver, new IntentFilter("time"));
        updateListReceiver = new UpdateListReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(updateListReceiver, new IntentFilter("updateList"));

        playPauseFirstReceiver = new ReceiverPlayPauseFirst();

        registerReceiver(this.playPauseFirstReceiver, new IntentFilter("PlayPauseFreq"));

        registerReceiver(this.broadcastReceiverPlayer, new IntentFilter("PlayPauseFreq"));

        WakeLocker.acquire(getApplicationContext());
    }

    protected void onStart() {
        super.onStart();
        ListView playListView = findViewById(R.id.play_list);

        mPlayItemAdapter = new PlayItemAdapter(this, playItems);
        mPlayItemAdapter.setIOnClickListener(new PlayItemAdapter.IOnClickListener() {
            @Override
            public void onPlayPauseClick(int position) {
                if (!checkBillingTime()) { return; }

                if (mPlayItemAdapter != null) {
                    int currentPositionPlay = mPlayItemAdapter.getCurrentPlayItemPosition();

                    if (position == currentPositionPlay) {
                        ivPlayPause.performClick();
                    } else {
                        if (position < playItems.size()) {
                            SharedPreferenceHelper.getSharedPreferences(FrequencyUIActivity.this).edit().putBoolean("isPlaying", true).apply();
                            mTvContentTextPlayPause.setVisibility(View.VISIBLE);
                            mTvContentText.setVisibility(View.VISIBLE);

                            mDatabase.updatePlaylistToPlayingFromId(playItems.get(position).getIdDB(), Math.ceil(mSharedPreferences.getInt("FreqDuration", 180)));
                            updateList();

                            ((ImageView) findViewById(R.id.pause_play_button)).setImageResource(R.drawable.ic_pause);
                            setPausePlayText(false);

                            if (!mPlayItemAdapter.isPausePlayer()) {
                                ivPlayPause.performClick();
                            } else {
                                Intent intent = new Intent("PauseFreq");
                                intent.putExtra("IS_PLAY_AGAIN", true);
                                sendBroadcast(intent);
                            }
                            mPlayItemAdapter.setCurrentPlayItem(position);
                            mPlayItemAdapter.setPausePlayer(true);
                        }
                    }
                }
            }
        });

        playListView.setAdapter(mPlayItemAdapter);
        mDatabase.getPlayList().moveToFirst();
        updateList();

        Cursor PlaylistItems = mDatabase.getPlayListItemsCursor();
        PlaylistItems.moveToFirst();
        if (PlaylistItems.getString(PlaylistItems.getColumnIndex("state")).equals("pause")) {
            if (mCurrentPlayItemPosition >= 0 && mCurrentPlayItemPosition < playItems.size()) {
                playItems.get(mCurrentPlayItemPosition).setCurrentDuration(mSharedPreferences.getInt("FreqDuration", 180) - timeInt);
            }
            mPlayItemAdapter.setPausePlayer(false);
        } else {
            if (isMyServiceRunning()) {
                if (mCurrentPlayItemPosition >= 0 && mCurrentPlayItemPosition < playItems.size()) {
                    playItems.get(mCurrentPlayItemPosition).setCurrentDuration(mSharedPreferences.getInt("FreqDuration", 180) - timeInt);
                }
                mPlayItemAdapter.setPausePlayer(false);
            } else {
                mPlayItemAdapter.setPausePlayer(true);
            }
        }
        PlaylistItems.close();
    }

    @Override
    protected void initComponents() {
        mTvContentTextPlayPause = findViewById(R.id.content_text_play_pause);
        mTvContentText = findViewById(R.id.content_text);
        mTvFrequencies = findViewById(R.id.tv_frequencies);
        ivRepeatController = findViewById(R.id.repeate_button);

        setTitle(getString(R.string.app_name));

        updateRepeatButton();

        checkBillingTime();
    }

    @Override
    protected void addListener() { showNavLeft(R.drawable.ic_back, new View.OnClickListener() {
            @Override
            public void onClick(View view) { finish(); }
        }); }

    public void repeat(View view) {
        int repeatType = SharedPreferenceHelper.getInstance(this).getInt(Constants.PREF_REPEAT_TYPE);
        if (repeatType == Constants.REPEAT_NONE) {
            SharedPreferenceHelper.getInstance(this).setInt(Constants.PREF_REPEAT_TYPE, Constants.REPEAT_ALL);
        } else if (repeatType == Constants.REPEAT_ALL) {
            SharedPreferenceHelper.getInstance(this).setInt(Constants.PREF_REPEAT_TYPE, Constants.REPEAT_ONE);
        } else {
            SharedPreferenceHelper.getInstance(this).setInt(Constants.PREF_REPEAT_TYPE, Constants.REPEAT_NONE);
        }
        updateRepeatButton();
    }

    public void updateRepeatButton() {
        int repeatType = SharedPreferenceHelper.getInstance(this).getInt(Constants.PREF_REPEAT_TYPE);
        if (repeatType == Constants.REPEAT_NONE) {
            ivRepeatController.setImageResource(R.drawable.ic_replay);
        } else if (repeatType == Constants.REPEAT_ALL) {
            ivRepeatController.setImageResource(R.drawable.ic_replay_selected);
        } else {
            ivRepeatController.setImageResource(R.drawable.ic_replay_1);
        }
    }

    public boolean checkBillingTime() {
        boolean purchased = SharedPreferenceHelper.getInstance(this).getBool(Constants.KEY_PURCHASED);
        if (purchased || (Calendar.getInstance().getTimeInMillis()
                - SharedPreferenceHelper.getInstance(this).getLong(Constants.PRE_LIMIT_TIME_PLAYER)
                > Constants.ONE_HOUR_TIME)) {
            return true;
        } else {
            long currentTotalTime = SharedPreferenceHelper.getInstance(this).getLong(Constants.FRE_PLAYER_FOR_FREE_TIME_5_MINTS);
            if (currentTotalTime >= Constants.LIMIT_TIME_5_MINUTES) { showPurchaseDialog(); }
        }
        return false;
    }

    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(this.freqStatusReceiver);
            unregisterReceiver(pauseReceiver);
            unregisterReceiver(timeReceiver);
            unregisterReceiver(updateListReceiver);
            unregisterReceiver(freqDataReceiver);
            unregisterReceiver(playPauseFirstReceiver);
            unregisterReceiver(broadcastReceiverPlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isMyServiceRunning() {
        for (RunningServiceInfo service : ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
            if ("com.zappkit.zappid.FrequencyService".equals(service.service.getClassName())) { return true; }
        }
        return false;
    }

    private void setData() {
        Cursor playlist = this.mDatabase.getPlayList();
        if (playlist.moveToFirst()) {
            String currentTitle = playlist.getString(playlist.getColumnIndex("sequence"));
            String value = playlist.getString(playlist.getColumnIndex("sequence"));

            try {
                double tmpFreq = Double.parseDouble(mDatabase.getFrequencyString(playlist
                        .getString(playlist.getColumnIndex("frequency_id")), playlist
                        .getInt(playlist.getColumnIndex("frequency_db_id"))));
                value = tmpFreq + " Hz";
            } catch (NumberFormatException ignored) { }

            playlist.close();

            ((TextView) findViewById(R.id.content_text)).setText(value);

            setTitle(currentTitle);

            if (SharedPreferenceHelper.getSharedPreferences(this).getBoolean("isPlaying", false)) {
                mTvContentTextPlayPause.setVisibility(View.VISIBLE);
                mTvContentText.setVisibility(View.VISIBLE);
            } else {
                if (currentTitle.equals("")) {
                    mTvContentTextPlayPause.setVisibility(View.INVISIBLE);
                    mTvContentText.setVisibility(View.VISIBLE);
                } else {
                    mTvContentTextPlayPause.setVisibility(View.INVISIBLE);
                    mTvContentText.setVisibility(View.INVISIBLE);
                }
            }

            if (currentTitle.equals("")) {
                mTvFrequencies.setText("");
            } else {
                mTvFrequencies.setText(getString(R.string.tv_frequencies));
            }
        }
    }

    public void setPausePlayText(boolean isPause) { mTvContentTextPlayPause.setText(isPause ? getString(R.string.txt_pause) : getString(R.string.tv_now_playing)); }

    public void pushStop(View view) {
        Cursor PlaylistItems = mDatabase.getPlayListItemsCursor();
        if (PlaylistItems.moveToFirst()) {
            if (PlaylistItems.getString(PlaylistItems.getColumnIndex("state")).equals("play")) {
                Intent intent = new Intent("PauseFreq");
                intent.putExtra("stop", true);
                sendBroadcast(intent);
            }
            PlaylistItems.close();
        }

        mDatabase.empty_playlist();

        if (this.mSharedPreferences.getBoolean("notifications", true)) { ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll(); }

        WakeLocker.release();

        finish();
    }

    public void push_pause_play(View view) {
        if (!checkBillingTime()) { return; }

        Cursor PlaylistItems = mDatabase.getPlayListItemsCursor();
        if (PlaylistItems.moveToFirst()) {
            if (PlaylistItems.getString(PlaylistItems.getColumnIndex("state")).equals("play")) {
                sendBroadcast(new Intent("PauseFreq"));
            } else {
                SharedPreferenceHelper.getSharedPreferences(this).edit().putBoolean("isPlaying", true).apply();
                mTvContentTextPlayPause.setVisibility(View.VISIBLE);
                mTvContentText.setVisibility(View.VISIBLE);

                mPlayIntent = new Intent(getBaseContext(), FrequencyService.class);
                startService(mPlayIntent);
                ((ImageView) findViewById(R.id.pause_play_button)).setImageResource(R.drawable.ic_pause);
                setPausePlayText(false);
                mPlayItemAdapter.setPausePlayer(true);
            }
            PlaylistItems.close();
        }
    }

    private String timeCalc(int sec) {
        int m = 0;
        int h = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while (sec >= 60) {
            m++;
            sec -= 60;
        }
        int s = sec;
        if (m >= 60) {
            while (m >= 60) {
                h++;
                m -= 60;
            }
            stringBuilder.append(h).append("h ");
            if (m == 0) {
                stringBuilder.append(m).append("m ");
            }
        }
        if (m != 0) {
            stringBuilder.append(m).append("m ");
        }
        stringBuilder.append(s).append("s ");
        return stringBuilder.toString();
    }

    public void updateList() {
        playItems.clear();
        Cursor playlist = mDatabase.getPlayList();
        if (playlist.moveToFirst()) {
            mCurrentPlayItemPosition = -1;
            int i = 0;
            do {
                PlayItemModel tempModel = new PlayItemModel();
                String tempId = playlist.getString(playlist.getColumnIndex("frequency_id"));
                boolean repeat = playlist.getInt(playlist.getColumnIndex("loop")) == 1;
                double tmpFreq = Double.parseDouble(mDatabase.getFrequencyString(tempId, playlist.getInt(playlist.getColumnIndex("frequency_db_id"))));
                tempModel.setId(Integer.parseInt(tempId));
                tempModel.setIdDB(playlist.getInt(playlist.getColumnIndex("_id")));
                tempModel.setLoop(repeat);
                tempModel.setFrequency(tmpFreq);
                tempModel.setParent(playlist.getString(playlist.getColumnIndex("sequence")));
                tempModel.setParentId(playlist.getInt(playlist.getColumnIndex("sequence_id")));
                tempModel.setSequenceDatabaseId(playlist.getInt(playlist.getColumnIndex("sequence_db_id")));
                tempModel.setPlayStatus(playlist.getInt(playlist.getColumnIndex("play_status")));
                if (mCurrentPlayItemPosition == -1
                        && tempModel.getPlayStatus() == 0
                        && SharedPreferenceHelper.getSharedPreferences(this).getBoolean("isPlaying", false)) {
                    mCurrentPlayItemPosition = i;
                }

                i++;

                playItems.add(tempModel);
            } while (playlist.moveToNext());
            playlist.close();

            totalTimeBaseValue = (mDatabase.getPlaylistCountPlaying() - 1) * mSharedPreferences.getInt("FreqDuration", 180);

            mPlayItemAdapter.setCurrentPlayItem(mCurrentPlayItemPosition);
            boolean purchased = SharedPreferenceHelper.getInstance(getApplicationContext()).getBool(Constants.KEY_PURCHASED);
            if (purchased) {
                mPlayItemAdapter.setOpenListType(PlayItemAdapter.OPEN_LIST_TYPE.OPEN_ALL);
            } else {
                mPlayItemAdapter.setOpenListType(PlayItemAdapter.OPEN_LIST_TYPE.OPEN_2_SONGS);
            }
            mPlayItemAdapter.notifyDataSetChanged();
        }
    }

    private void adjustLanguage() {
        Locale locale = new Locale(mSharedPreferences.getString("language", "en"));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_frequency_ui);
    }

    class FreqStatusReceiver extends BroadcastReceiver {
        FreqStatusReceiver() { }

        public void onReceive(Context context, Intent intent) {
            if (!mDatabase.getNextFreqOnPlayList().equals("done!")) { updateList(); }
            else { finish(); }
        }
    }

    class PauseReceiver extends BroadcastReceiver {
        PauseReceiver() { }

        public void onReceive(Context context, Intent intent) {
            if (!intent.hasExtra("IS_PLAY_AGAIN")) {
                ((ImageView) FrequencyUIActivity.this.findViewById(R.id.pause_play_button)).setImageResource(R.drawable.ic_play);
                setPausePlayText(true);
                mPlayItemAdapter.setPausePlayer(false);
            }
        }
    }

    class ReceiverPlayPauseFirst extends BroadcastReceiver {
        ReceiverPlayPauseFirst() { }

        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("play_status")) {
                if (intent.getStringExtra("play_status").equals("play")) {
                    ((ImageView) FrequencyUIActivity.this.findViewById(R.id.pause_play_button)).setImageResource(R.drawable.ic_pause);
                    setPausePlayText(false);
                } else {
                    ((ImageView) FrequencyUIActivity.this.findViewById(R.id.pause_play_button)).setImageResource(R.drawable.ic_play);
                    setPausePlayText(true);
                }
            }
        }
    }

    class FreqDataReceiver extends BroadcastReceiver {
        FreqDataReceiver() { }

        public void onReceive(Context context, Intent intent) {
            if (mCurrentPlayItemPosition == -1) { updateList(); }

            int freqDuration = mSharedPreferences.getInt("FreqDuration", 180);
            totalTimeBaseValue = (mDatabase.getPlaylistCount() - 1) * freqDuration;
            Cursor playlist = mDatabase.getPlayListItemsCursor();
            playlist.moveToFirst();

            double tmpFreq = Double.parseDouble(mDatabase.getFrequencyString(playlist.getString(playlist.getColumnIndex("frequency_id")), playlist.getInt(playlist.getColumnIndex("frequency_db_id"))));
            String current = tmpFreq + " Hz";

            playlist.close();
            ((TextView) FrequencyUIActivity.this.findViewById(R.id.content_text)).setText(current);
        }
    }

    class UpdateListReceiver extends BroadcastReceiver {
        UpdateListReceiver() { }

        public void onReceive(Context context, Intent intent) {
            updateList();
        }
    }

    private BroadcastReceiver broadcastReceiverPlayer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean purchased = SharedPreferenceHelper.getInstance(getApplicationContext()).getBool(Constants.KEY_PURCHASED);
            if (mPlayItemAdapter != null) {
                if (purchased) {
                    mPlayItemAdapter.setOpenListType(PlayItemAdapter.OPEN_LIST_TYPE.OPEN_ALL);
                } else {
                    mPlayItemAdapter.setOpenListType(PlayItemAdapter.OPEN_LIST_TYPE.OPEN_2_SONGS);
                }
            }
        }
    };
}
