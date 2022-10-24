package com.zappkit.zappid.lemeor.top_menu;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.zappkit.zappid.lemeor.main_menu.player.FrequencyService;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.zappkit.zappid.lemeor.base.BaseActivity;
import com.zappkit.zappid.lemeor.subscription.SubscribeActivity;
import com.zappkit.zappid.lemeor.subscription.SubscriptionInAppActivity;
import com.zappkit.zappid.lemeor.tools.Constants;
import com.zappkit.zappid.lemeor.tools.Utilities;
import com.zappkit.zappid.views.CustomDialogInputFrequency;
import com.zappkit.zappid.views.CustomDialogMessageConfirm;
import com.zappkit.zappid.views.CustomDialogMessageDuration;

public class SettingActivity extends BaseActivity implements View.OnClickListener {
    private View mBtnFrequency, mBtnNotification, mBtnCalibration;
    private SharedPreferences.Editor editor;
    private SharedPreferences settings;
    private int mFreqDuration;
    private boolean isEnableNotify;
    private TextView mTvTitle, mTvSecondTime;
    private CheckBox mCbNotification;
//    private DbHelper dbHelper;
    private int MAX_ACTIVITY_COUNT_UNLIMITED = 4;
    private Button mBtnFreeTrial, mBtnRestore, mBtnSubcribe;
    private View mViewActionPurchase;
    private ImageView mImvLocked;

    private BroadcastReceiver mBroadcastReceiverPurchase = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkPurchasedApp();
        }
    };

    @Override
    protected int initLayout() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initComponents() {
//        dbHelper = new DbHelper(this);
        registerReceiver(mBroadcastReceiverPurchase, new IntentFilter(Constants.BROADCAST_ACTION_PURCHASED));
        settings = SharedPreferenceHelper.getSharedPreferences(this);
        editor = settings.edit();
        mFreqDuration = settings.getInt("FreqDuration", 180);
        isEnableNotify = settings.getBoolean("notifications", true);
        mBtnFrequency = findViewById(R.id.btn_frequency);
        mBtnNotification = findViewById(R.id.btn_notification);
        mBtnCalibration = findViewById(R.id.btn_calibration);
        mCbNotification = findViewById(R.id.checkbox);
        mTvSecondTime = findViewById(R.id.tv_second_time);
        mTvTitle = findViewById(R.id.tv_title);
        mTvTitle.setText("SETTINGS");
        setTime(mFreqDuration);
        mCbNotification.setChecked(isEnableNotify);

        mBtnFreeTrial = findViewById(R.id.btn_free_trial);
        mBtnRestore = findViewById(R.id.btn_restore_purchase);
        mBtnSubcribe = findViewById(R.id.btn_subcribe);
        mImvLocked = findViewById(R.id.imv_locked);

        mViewActionPurchase = findViewById(R.id.view_action);

        checkPurchasedApp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiverPurchase);
    }

    public void checkPurchasedApp(){
        if (SharedPreferenceHelper.getInstance(this).getBool(Constants.KEY_PURCHASED)) {
            mViewActionPurchase.setVisibility(View.GONE);
            mImvLocked.setVisibility(View.INVISIBLE);
        } else {
            mViewActionPurchase.setVisibility(View.VISIBLE);
            mImvLocked.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void addListener() {
        mBtnFrequency.setOnClickListener(this);
        mBtnNotification.setOnClickListener(this);
        mBtnCalibration.setOnClickListener(this);

        mBtnFreeTrial.setOnClickListener(this);
        mBtnRestore.setOnClickListener(this);
        mBtnSubcribe.setOnClickListener(this);
        hiddenNavRight();
        showNavLeft(R.drawable.ic_back, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setTime(int second) {
        if (second > 1) {
            mTvSecondTime.setText(second + " Seconds");
        } else {
            mTvSecondTime.setText(second + " Second");
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_frequency:
                if (SharedPreferenceHelper.getInstance(SettingActivity.this).getBool(Constants.KEY_PURCHASED)) {
                    CustomDialogInputFrequency inputFrequency = new CustomDialogInputFrequency(this);
                    final CustomDialogMessageDuration messageDuration = new CustomDialogMessageDuration(this);
                    inputFrequency.show();
                    inputFrequency.setTextInput(settings.getInt("FreqDuration", 180));
                    inputFrequency.setItemSelectedListener(new CustomDialogInputFrequency.IItemSelectedListener() {
                        @Override
                        public void onSelected(final String value) {
                            messageDuration.show();
                            messageDuration.setmOnClickYesListener(new CustomDialogMessageDuration.OnClickYesListener() {
                                @Override
                                public void onClick(String value1) {
                                    int valueNumber = Integer.parseInt(value.equals("") ? "0" : value);
                                    setTime(valueNumber);
                                    editor.putInt("FreqDuration", valueNumber);
                                    editor.commit();

                                    Utilities.stopPlayerInSetting(SettingActivity.this);
                                }
                            });
                        }
                    });
                } else {
                    showSubscriptionAppDialog();
                }
                break;
            case R.id.btn_notification:
                if (isEnableNotify) {
                    isEnableNotify = false;
                    mCbNotification.setChecked(isEnableNotify);
                } else {
                    isEnableNotify = true;
                    mCbNotification.setChecked(isEnableNotify);
                }
                editor.putBoolean("notifications", isEnableNotify);
                editor.commit();
                break;
            case R.id.btn_calibration:
                CustomDialogMessageConfirm messageConfirm = new CustomDialogMessageConfirm(this);
                messageConfirm.show();
                messageConfirm.setTextTitle("Before you begin…");
                messageConfirm.setTextContent(getString(R.string.prefCaliDialogFirstText));

                messageConfirm.setOnClickConfirmListener(new CustomDialogMessageConfirm.OnClickConfirmListener() {
                    @Override
                    public void onClick(String value) {
                        if (isMyServiceRunning()) {
                            stopFrequencyService();
                        }
                        playLowFrequency();
                        final CustomDialogMessageConfirm messageConfirm = new CustomDialogMessageConfirm(SettingActivity.this);
                        messageConfirm.setCanDismiss(false);
                        messageConfirm.show();
                        messageConfirm.setTextButtonLeft(getString(R.string.prefCaliDialogSecondNegButton));
                        messageConfirm.setTextButtonRight(getString(R.string.prefCaliDialogSecondPosButton));
                        messageConfirm.setTextTitle("Calibrating…");
                        messageConfirm.setTextContent(getString(R.string.prefCaliDialogSecondText));

                        final BroadcastReceiver[] receiver = new BroadcastReceiver[1];
                        messageConfirm.setOnClickConfirmListener(new CustomDialogMessageConfirm.OnClickConfirmListener() {
                            @Override
                            public void onClick(String value) {
                                LocalBroadcastManager.getInstance(SettingActivity.this).unregisterReceiver(receiver[0]);
                                stopFrequencyService();
                                messageConfirm.dismiss();
                            }
                        });
                        messageConfirm.setOnClickCancelListener(new CustomDialogMessageConfirm.OnClickCancelListener() {
                            class C01871 extends BroadcastReceiver {
                                C01871() {
                                }

                                public void onReceive(Context context, Intent intent) {
                                    messageConfirm.setTextContent(getResources().getString(R.string.prefCaliDialogSecondTextTwo));
                                    LocalBroadcastManager.getInstance(SettingActivity.this).unregisterReceiver(this);
                                    if (settings.getFloat("freqSign", 1.0f) == 1.0f) {
                                        editor.putFloat("freqSign", -1.0f);
                                        editor.commit();
                                    } else {
                                        editor.putFloat("freqSign", 1.0f);
                                        editor.commit();
                                    }
                                    playLowFrequency();
                                }
                            }

                            @Override
                            public void onClick() {
                                if (isMyServiceRunning()) {
                                    stopFrequencyService();
                                    receiver[0] = new C01871();
                                    LocalBroadcastManager.getInstance(SettingActivity.this).registerReceiver(receiver[0], new IntentFilter("FreqServiceStopped"));
                                    return;
                                }
                                clearFrequencyPlaylist();
                                playLowFrequency();
                            }
                        });
                    }
                });
                break;
            case R.id.btn_free_trial:
//                startActivity(new Intent(this, SubscriptionInAppActivity.class));
                Intent intent = new Intent(SettingActivity.this, SubscriptionInAppActivity.class);
                intent.putExtra(Constants.IN_APP_TYPE, Constants.ENUM_FREE);
                startActivity(intent);
                break;
            case R.id.btn_restore_purchase:
                break;
            case R.id.btn_subcribe:
                startActivity(new Intent(this, SubscribeActivity.class));
                break;
        }
    }

    private boolean isMyServiceRunning() {
        for (ActivityManager.RunningServiceInfo service : ((ActivityManager) getSystemService(ACTIVITY_SERVICE))
                .getRunningServices(Integer.MAX_VALUE)) {
            if ("com.zappkit.zappid.FrequencyService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

//    private boolean isMyServiceRunning() {
//        for (ActivityManager.RunningServiceInfo service : ((ActivityManager) getSystemService("activity")).getRunningServices(MAX_ACTIVITY_COUNT_UNLIMITED)) {
//            if ("com.zappkit.zappid.FrequencyService".equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }

    @SuppressLint("WrongConstant")
    private void stopFrequencyService() {
        //LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent("PauseFreq");
        intent.putExtra("stop", true);
        sendBroadcast(intent);

        clearFrequencyPlaylist();
        if (this.settings.getBoolean("notifications", true)) {
            ((NotificationManager) getSystemService("notification")).cancelAll();
        }
    }

    private void clearFrequencyPlaylist() {

        mDatabase.empty_playlist();
    }

    private void playLowFrequency() {
        mDatabase.addToPlaylist("50", 1, getResources().getString(R.string.title_activity_frequency), -1, 1);
        startService(new Intent(this, FrequencyService.class));
//        FrequencyJobService.enqueueWork(this, new Intent(this, FrequencyJobService.class));
    }
}
