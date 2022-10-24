package com.zappkit.zappid.lemeor.main_menu.player;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.zappkit.zappid.lemeor.tools.Constants;

import java.util.Calendar;

public class FrequencyService extends IntentService {
    private int BETWEEN_TIME_CHECK_BILLING = 200;
    AudioTrack audioTrack;
    LocalBroadcastManager broadcaster;
    DbHelper database;
    private int duration;
    private double freqOfTone;
    private float freqSign;
    private byte[] generatedSnd;
    private int mId;
    MyPhoneStateListener mPhoneStateListener;
    TelephonyManager mTelephonyManager;
    private int numSamples;
    private String playState = "play";
    BroadcastReceiver receiver;
    private double[] sample;
    private int sampleRate;
    SharedPreferences settings;
    private Boolean stop = Boolean.valueOf(false);
    private boolean mIsPlayAgain = false;
    private boolean isRequestAudioFocus = true;
    private AudioManager mAudioManager;
    private PowerManager.WakeLock wakeLock;

    class C01351 extends BroadcastReceiver {
        C01351() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("IS_PLAY_AGAIN")) {
                mIsPlayAgain = intent.getBooleanExtra("IS_PLAY_AGAIN", false);
            } else {
                FrequencyService.this.stop = Boolean.valueOf(intent.getBooleanExtra("stop", false));
            }
            FrequencyService.this.playState = "pause";
        }
    }

    private Handler mHandlerPlayForFreeTime = new Handler();
    private Runnable mRunnablePlayForFreeTime = new Runnable() {
        @Override
        public void run() {
            checkExpireTimeForBilling();
            mHandlerPlayForFreeTime.postDelayed(mRunnablePlayForFreeTime, BETWEEN_TIME_CHECK_BILLING);
        }
    };

    private class MyPhoneStateListener extends PhoneStateListener {
        LocalBroadcastManager broadcaster;

        private MyPhoneStateListener() {
            this.broadcaster = LocalBroadcastManager.getInstance(null);
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case 0:
                    Log.d("DEBUG", "IDLE");
                    return;
                case 1:
                    if (!FrequencyService.this.database.getNextFreqOnPlayList().equals("done!")) {
                        sendBroadcast(new Intent("PauseFreq"));//this.broadcaster.
                    }
                    Log.d("DEBUG", "RINGING");
                    return;
                case 2:
                    Log.d("DEBUG", "OFFHOOK");
                    return;
                default:
                    return;
            }
        }
    }

    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                // Pause the playback
                if (!FrequencyService.this.database.getNextFreqOnPlayList().equals("done!")) {
                    sendBroadcast(new Intent("PauseFreq"));//this.broadcaster.
                }
            }
        }
    }

    private BecomingNoisyReceiver myNoisyAudioStreamReceiver;

    public FrequencyService() {
        super("FrequencyService");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        setPlayPauseFrist();
        return super.onStartCommand(intent, flags, startId);
    }

    public void setPlayPauseFrist() {
        Intent i = new Intent("PlayPauseFreq");
        i.putExtra("play_status", playState);
        sendBroadcast(i);
    }

    public void onCreate() {
        this.settings = SharedPreferenceHelper.getSharedPreferences(this);
        this.database = new DbHelper(this);
        this.broadcaster = LocalBroadcastManager.getInstance(this);
        super.onCreate();
        this.receiver = new C01351();
        registerReceiver(this.receiver, new IntentFilter("PauseFreq"));//LocalBroadcastManager.getInstance(this).

        this.mPhoneStateListener = new MyPhoneStateListener();
        this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
        this.freqSign = this.settings.getFloat("freqSign", 1.0f);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::FrequencyService");
        wakeLock.acquire();

        Log.d("LOG", "Wakelock acquired");
    }

    public void requestToSilentOtherMusicApps() {
        isRequestAudioFocus = false;
        mAudioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {

                                            @Override
                                            public void onAudioFocusChange(int i) {
                                                switch (i) {
                                                    case AudioManager.AUDIOFOCUS_GAIN:
                                                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                                        isRequestAudioFocus = false;  // Resume your media player here
                                                        break;
                                                    case AudioManager.AUDIOFOCUS_LOSS:
                                                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                                        sendBroadcast(new Intent("PauseFreq"));
                                                        isRequestAudioFocus = true; // Pause your media player here
                                                        break;
                                                }
                                            }
                                        },
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public void checkExpireTimeForBilling() {
        long totalTime = SharedPreferenceHelper.getInstance(this).getLong(Constants.FRE_PLAYER_FOR_FREE_TIME_5_MINTS);
        if (totalTime > Constants.LIMIT_TIME_5_MINUTES) {
            mHandlerPlayForFreeTime.removeCallbacksAndMessages(null);
            playState = "pause";
            Intent intent = new Intent(Constants.ACTION_PLAYER_FOR_FREE_TIME_5_MINTS);
            sendBroadcast(intent);
        } else {
            Log.d("Current Time", totalTime / 1000 / 60 + " : " + ((totalTime / 1000) % 60));
        }
    }

    //    protected void onHandleWork(Intent intent) {
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.e("LOG", "onHandleIntent");

        sendBroadcast(new Intent(Constants.BROADCAST_PLAY_PAUSE_CONTROLLER_GLOBAL));

        boolean purchased = SharedPreferenceHelper.getInstance(this).getBool(Constants.KEY_PURCHASED);
        String id = this.database.getNextFreqOnPlayList();
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (!id.equals("done!")) {
            String notificationChanelId = "FireFrequencyNotificationChanelID";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(notificationChanelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setSound(null, null);
                mNotificationManager.createNotificationChannel(channel);
            }
            Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), notificationChanelId);
            sendFreqData();

            Cursor thisplayitem = this.database.getPlayListItemsCursor();
            thisplayitem.moveToFirst();
            String databasestate = thisplayitem.getString(thisplayitem.getColumnIndex("state"));
            Integer timeleft = Integer.valueOf(thisplayitem.getInt(thisplayitem.getColumnIndex("time_lapsed")));
            int freq_db_id = thisplayitem.getInt(thisplayitem.getColumnIndex("frequency_db_id"));
            thisplayitem.close();
            Integer prefduration = Integer.valueOf(this.settings.getInt("FreqDuration", 180));
            if (databasestate.equals("pause")) {
                this.duration = timeleft.intValue();
                this.database.ChangeFirstItemState("play", 0.0d);
                if (this.duration < 0) {
                    this.duration = 0;
                }
            } else {
                this.duration = prefduration.intValue();
            }

            Cursor freq = this.database.getFrequency(id, freq_db_id);
            freq.moveToFirst();
            this.freqOfTone = Double.parseDouble(freq.getString(freq.getColumnIndex("frequency")).replace("146713.583 W1 G0 A20 O0", "146713.583").replace("70D50", "7050"));

            if (this.settings.getBoolean("notifications", true)) {
                mBuilder.setSmallIcon(R.drawable.ic_zapp_notify)
                        .setContentTitle(getResources().getString(R.string.serviceNotiCurrPlay))
                        .setContentText(getResources().getString(R.string.freq_hz_cursor) + " " + this.freqOfTone + " Hz")
                        .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), FrequencyUIActivity.class), 134217728));
                startForeground(1997, mBuilder.build());
            }
            freq.close();
            this.database.frequencyPlayed(id);

            freqPrep(this.freqOfTone);
            generateSound(100);
            playSound();

            int refreshRateMilliSeconds = 1000 / 10;

            setPlayPauseFrist();

            for (int i = 0; i < this.duration * 10; i++) {
                SystemClock.sleep((long) refreshRateMilliSeconds);
                if (this.playState.equals("pause")) {
                    this.duration -= i / 10;
                    break;
                }
                if (!purchased) {
                    long currentTotalTime = SharedPreferenceHelper.getInstance(this).getLong(Constants.FRE_PLAYER_FOR_FREE_TIME_5_MINTS);
                    SharedPreferenceHelper.getInstance(this).setLong(Constants.FRE_PLAYER_FOR_FREE_TIME_5_MINTS, currentTotalTime + refreshRateMilliSeconds);
                    if (currentTotalTime + refreshRateMilliSeconds >= Constants.LIMIT_TIME_5_MINUTES) {
                        break;
                    }
                }
                if (i % 10 == 0) {
                    sendtime(this.duration - (i / 10));
                }
            }

            terminate();

            if (!this.playState.equals("pause") || mIsPlayAgain) {
                if (!this.database.getNextFreqOnPlayList().equals("done!")) {
                    if (this.database.shouldLoop()) {
                        this.database.loopFrequency();
                    }

                    if (mIsPlayAgain) {
                        playState = "play";
                        mIsPlayAgain = false;
                    } else {
                        purchased = SharedPreferenceHelper.getInstance(this).getBool(Constants.KEY_PURCHASED);
                        int typeRepeate = SharedPreferenceHelper.getInstance(this).getInt(Constants.PREF_REPEAT_TYPE);
                        if (typeRepeate == Constants.REPEAT_NONE) {
                            this.database.DeletePayedItemFromPlayList();
                        } else if (typeRepeate == Constants.REPEAT_ALL) {
                            int playingCount = this.database.getPlaylistCountPlaying();
                            if (playingCount <= 1) {
                                this.database.updatePlaylistToPlaying();
                            } else {
                                this.database.DeletePayedItemFromPlayList();
                            }
                        }

                        //Check for un-puchased
                        if (!purchased && typeRepeate != Constants.REPEAT_ONE) {
                            if (this.database.getPlaylistCountPlayed() >= 2) {
                                if (typeRepeate == Constants.REPEAT_ALL) {
                                    this.database.updatePlaylistToPlaying();
                                } else {
                                    this.database.updatePlaylistToPlayed();
                                }
                            }
                        }
                    }

                    if (purchased || SharedPreferenceHelper.getInstance(this).getLong(Constants.FRE_PLAYER_FOR_FREE_TIME_5_MINTS) < Constants.LIMIT_TIME_5_MINUTES) {
                        startService(new Intent(this, FrequencyService.class));
                    } else {
                        this.database.updatePlaylistToPlaying();
                        SharedPreferenceHelper.getSharedPreferences(this).edit().putBoolean("isPlaying", false).commit();
                        this.database.ChangeFirstItemState("pause", Math.ceil((double) this.duration));

                        SharedPreferenceHelper.getInstance(this).setLong(Constants.PRE_LIMIT_TIME_PLAYER, Calendar.getInstance().getTimeInMillis());//limit 1hour = 60*60*1000
                        Log.d("A_PRE_LIMIT_TIME_PLAYER", SharedPreferenceHelper.getInstance(this).getLong(Constants.PRE_LIMIT_TIME_PLAYER) + "");
                        sendBroadcast(new Intent(Constants.BROADCAST_SHOW_DIALOG_IN_APP));
                        playState = "pause";
                        setPlayPauseFrist();
                    }
                }
                sendResult();
            } else if (this.stop.booleanValue()) {
                broadcastFrequencyStopped();
            } else {
                this.database.ChangeFirstItemState("pause", Math.ceil((double) this.duration));
                if (this.settings.getBoolean("notifications", true)) {
                    mBuilder.setSmallIcon(R.drawable.ic_zapp_notify)
                            .setContentTitle(getResources().getString(R.string.serviceNotiCurrPause))
                            .setContentText(getResources().getString(R.string.freq_hz_cursor) + " " + this.freqOfTone + " Hz")
                            .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), FrequencyUIActivity.class), 134217728));
                    startForeground(1997, mBuilder.build());
                }
            }
        } else if (this.settings.getBoolean("notifications", true)) {
            mNotificationManager.cancelAll();
        }
    }

    private void broadcastFrequencyStopped() {
        Log.e("LOG", "broadcastFrequencyStopped");

        this.broadcaster.sendBroadcast(new Intent("FreqServiceStopped"));
    }

    public void sendResult() {
        this.broadcaster.sendBroadcast(new Intent("Freq_status"));
    }

    private void sendtime(int time) {
        Log.e("LOg", "time " + time);

        Intent intent = new Intent("time");
        intent.putExtra("current", time);
        broadcaster.sendBroadcast(intent);
    }

    private void sendFreqData() {
        this.broadcaster.sendBroadcast(new Intent("FreqData"));
    }

    protected void freqPrep(double freq) {
        Log.e("LOG", "!!! freqPrep !!!");

        double decimalPlaces = getDecimalPlaces(freq);
        String decimalString = Double.toString(getDecimalPlaces(freq));
        double baseFreq;

        Log.e("LOG", "decimalPlaces " + decimalPlaces);
        Log.e("LOG", "decimalString " + decimalString);

        if (freq < 100.0d) {
            if (decimalPlaces == 0.0d) {
                baseFreq = freq;
            } else if (decimalString.length() == 3) {
                if (decimalPlaces == 0.3d || decimalPlaces == 0.7d || decimalPlaces == 0.9d) {
                    baseFreq = freq * 10.0d;
                } else if (decimalPlaces == 0.4d || decimalPlaces == 0.6d || decimalPlaces == 0.8d) {
                    baseFreq = freq * 5.0d;
                } else {
                    baseFreq = ((double) ((int) (1.0d / decimalPlaces))) * freq;
                }
            } else if (decimalString.length() >= 4) {
                baseFreq = (double) ((int) (100.0d * freq));
            } else {
                Log.e("FrequencyGenerator", "Frequency is lower than 100 but doesn't fall in any sub-category (length) " + decimalString);
                baseFreq = 0.0d;
            }

            createSampleRate(baseFreq);

            if (freq < 7.0d) {
                genPulseTone();
            } else {
                genSquareTone();
            }
        } else if (decimalPlaces == 0.0d) {
            createSampleRate(freq);
            genSquareTone();
        } else if (decimalString.length() == 3) {
            if (decimalPlaces == 0.3d || decimalPlaces == 0.7d || decimalPlaces == 0.9d) {
                baseFreq = freq * 10.0d;
            } else if (decimalPlaces == 0.4d || decimalPlaces == 0.6d || decimalPlaces == 0.8d) {
                baseFreq = freq * 5.0d;
            } else {
                baseFreq = ((double) ((int) (1.0d / decimalPlaces))) * freq;
            }
            if (createSampleRate(baseFreq)) {
                genSquareTone();
            } else {
                genSineTone();
            }
        } else {
            genSineTone();
        }
    }

    protected boolean createSampleRate(double baseFreq) {
        if (baseFreq <= 6.0d) {
            this.sampleRate = (int) (8000.0d * baseFreq);
        } else if (baseFreq <= 14.0d) {
            this.sampleRate = (int) (2000.0d * baseFreq);
        } else if (baseFreq <= 48.0d) {
            this.sampleRate = (int) (1000.0d * baseFreq);
        } else if (baseFreq <= 192.0d) {
            this.sampleRate = (int) (250.0d * baseFreq);
        } else if (baseFreq <= 960.0d) {
            this.sampleRate = (int) (50.0d * baseFreq);
        } else if (baseFreq <= 4800.0d) {
            this.sampleRate = (int) (10.0d * baseFreq);
        } else if (baseFreq <= 6000.0d) {
            this.sampleRate = (int) (8.0d * baseFreq);
        } else if (baseFreq <= 8000.0d) {
            this.sampleRate = (int) (6.0d * baseFreq);
        } else if (baseFreq <= 12000.0d) {
            this.sampleRate = (int) (4.0d * baseFreq);
        } else if (baseFreq <= 24000.0d) {
            this.sampleRate = (int) (2.0d * baseFreq);
        } else {
            Log.e("FrequencyGenerator", "Base Frequency is too high to be processed");
            return false;
        }
        return true;
    }

    protected void genSineTone() {
        Log.e("LOG", "genSineTone");

        this.sampleRate = 44100;
        this.numSamples = this.sampleRate * 20;
        this.sample = new double[this.numSamples];
        this.generatedSnd = new byte[(this.numSamples * 2)];
        for (int i = 0; i < this.numSamples; i++) {
            this.sample[i] = Math.sin((6.283185307179586d * ((double) i)) / (((double) this.sampleRate) / this.freqOfTone));
        }
    }

    protected void genSquareTone() {
        Log.e("LOG", "genSquareTone");

        int switchRate = (int) ((((double) this.sampleRate) / this.freqOfTone) / 2.0d);
        this.numSamples = this.sampleRate * 12;
        this.sample = new double[this.numSamples];
        this.generatedSnd = new byte[(this.numSamples * 2)];
        Integer y = Integer.valueOf(0);
        Integer freq = Integer.valueOf(1);
        for (int i = 0; i < this.numSamples; i++) {
            if (y.intValue() == switchRate) {
                int i2;
                if (freq.intValue() == 1) {
                    i2 = -1;
                } else {
                    i2 = 1;
                }
                freq = Integer.valueOf(i2);
                y = Integer.valueOf(0);
            }
            this.sample[i] = (double) freq.intValue();
            y = Integer.valueOf(y.intValue() + 1);
        }
    }

    protected void genPulseTone() {
        Log.e("LOG", "genPulseTone");

        int switchRate = (int) (((double) this.sampleRate) / this.freqOfTone);
        this.numSamples = switchRate * 8;
        this.sample = new double[this.numSamples];
        this.generatedSnd = new byte[(this.numSamples * 2)];
        int saw = switchRate;
        Integer y = Integer.valueOf(0);
        for (int i = 0; i < this.numSamples; i++) {
            if (y.intValue() == switchRate) {
                saw = switchRate;
                y = Integer.valueOf(0);
            }
            this.sample[i] = (((((double) (((float) saw) / ((float) switchRate))) * 20.0d) / 10.0d) - 1.0d) * ((double) this.freqSign);
            saw--;
            y = Integer.valueOf(y.intValue() + 1);
        }
    }

    protected void genDoubleSquare() {
        int switchRate = (int) (((double) this.sampleRate) / this.freqOfTone);
        int holdSamples = (int) Math.ceil(((double) this.sampleRate) * 0.02d);
        this.numSamples = switchRate * 8;
        this.sample = new double[this.numSamples];
        this.generatedSnd = new byte[(this.numSamples * 2)];
        Integer y = Integer.valueOf(0);
        int i = 0;
        while (i < this.numSamples) {
            if (y.intValue() == switchRate) {
                y = Integer.valueOf(0);
            }
            if (y.intValue() == 0) {
                for (int hold = 0; hold < holdSamples; hold++) {
                    this.sample[i] = -1.0d;
                    i++;
                    y = Integer.valueOf(y.intValue() + 1);
                }
            } else {
                this.sample[i] = 1.0d;
                y = Integer.valueOf(y.intValue() + 1);
            }
            i++;
        }
    }

    protected void genSlowSquare() {
        int switchRate = (int) ((((double) this.sampleRate) / this.freqOfTone) / 2.0d);
        this.numSamples = this.sampleRate * 12;
        this.sample = new double[this.numSamples];
        this.generatedSnd = new byte[(this.numSamples * 2)];
        Integer y = Integer.valueOf(0);
        Integer freq = Integer.valueOf(1);
        for (int i = 0; i < this.numSamples; i++) {
            if (y.intValue() == switchRate) {
                int i2;
                if (freq.intValue() == 1) {
                    i2 = -1;
                } else {
                    i2 = 1;
                }
                freq = Integer.valueOf(i2);
                y = Integer.valueOf(0);
            }
            this.sample[i] = (double) freq.intValue();
            y = Integer.valueOf(y.intValue() + 1);
        }
    }

    protected void generateSound(int vol) {
//        int volume = (int) ((((double) vol) / 100.0d) * 32767.0d);
//        int idx = 0;
//        for (double dVal : this.sample) {
//            short val = (short) ((int) (((double) volume) * dVal));
//            int i = idx + 1;
//            this.generatedSnd[idx] = (byte) (val & MotionEventCompat.ACTION_MASK);
//            idx = i + 1;
//            this.generatedSnd[i] = (byte) ((MotionEventCompat.ACTION_POINTER_INDEX_MASK & val) >>> 8);
//        }
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / ((double) sampleRate / Math.round(freqOfTone)));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (double dVal : sample) {
            short val = (short) (dVal * 32767);
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
    }

    protected void playSound() {
//        try {
//            this.audioTrack = new AudioTrack(3, this.sampleRate, 2, 2, this.numSamples, 0);
//            this.audioTrack.write(this.generatedSnd, 0, this.generatedSnd.length);
//            this.audioTrack.reloadStaticData();
//            this.audioTrack.setLoopPoints(0, this.numSamples / 2, -1);
//
//            this.audioTrack.play();
//
//            AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
//            if (mAudioManager.isMusicActive()) {
//                Intent i = new Intent("com.android.music.musicservicecommand");
//                i.putExtra("command", "pause");
//                sendBroadcast(i);
//            }
//        } catch (IllegalStateException e) {
//
//        } catch (IllegalArgumentException ex) {
//
//        }

        try {
            this.audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    numSamples,
                    AudioTrack.MODE_STATIC);
            this.audioTrack.write(generatedSnd, 0, generatedSnd.length);
//            this.audioTrack.reloadStaticData();
            this.audioTrack.setLoopPoints(0, numSamples / 2, -1);

            //PLAY

            try {
                audioTrack.setStereoVolume(0, 0);
                this.audioTrack.play();
                Thread.sleep(100);
                audioTrack.setStereoVolume(1, 1);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            if (mAudioManager.isMusicActive()) {
                Intent i = new Intent("com.android.music.musicservicecommand");
                i.putExtra("command", "pause");
                sendBroadcast(i);
            }
        } catch (IllegalStateException e) {

        } catch (IllegalArgumentException ex) {

        }
    }

    public double getDecimalPlaces(double num) {
        String text = Double.toString(Math.abs(num));
        double multiplier = Math.pow(10.0d, (double) ((text.length() - text.indexOf(46)) - 1));
        return ((num * multiplier) - (Math.floor(num) * multiplier)) / multiplier;
    }

    //TERMINATE
    protected void terminate() {
        Log.e("LOG", "terminate");

        try {
            audioTrack.setStereoVolume(0, 0);
            Thread.sleep(100);
            this.audioTrack.pause();
            this.audioTrack.flush();
            this.audioTrack.release();
            Thread.sleep(100);
            audioTrack.setStereoVolume(1, 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        Log.e("LOG", "onDestroy");

        SharedPreferenceHelper.getSharedPreferences(this).edit().putBoolean("isPlaying", false).commit();

        mIsPlayAgain = false;
        stop = true;
        playState = "pause";

        sendBroadcast(new Intent("PauseFreq"));

        terminate();

        super.onDestroy();

        unregisterReceiver(receiver);
        wakeLock.release();
        System.gc();

        this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        this.mPhoneStateListener = null;

        mHandlerPlayForFreeTime.removeCallbacksAndMessages(null);
    }
}
