package com.zappkit.zappid.lemeor;

import android.Manifest;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.appsflyer.AppsFlyerLib;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.zappkit.zappid.BuildConfig;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.api.ApiListener;
import com.zappkit.zappid.lemeor.api.models.GetAPKsNewVersionOutput;
import com.zappkit.zappid.lemeor.api.models.GetFlashSaleOutput;
import com.zappkit.zappid.lemeor.base.BaseActivity;
import com.zappkit.zappid.lemeor.main_menu.MainActivity;
import com.zappkit.zappid.lemeor.api.tasks.BaseTask;
import com.zappkit.zappid.lemeor.api.tasks.GetAPKNewVersionTask;
import com.zappkit.zappid.lemeor.api.tasks.GetFlashSaleTask;
import com.zappkit.zappid.lemeor.tools.Constants;
import com.zappkit.zappid.lemeor.tools.QcAlarmManager;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import com.zappkit.zappid.lemeor.tools.Utilities;
import com.zappkit.zappid.views.CustomDialogMessageConfirm;
import com.zappkit.zappid.views.CustomFontTextView;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

public class MainMenuActivity extends BaseActivity implements ApiListener {

    public static final String IN_APP = "IN_APP_TYPE";

    public static boolean ourMainRunning = false;
    private String[] PERMISSIONS = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private boolean isRequestAudioFocus = true;
    private AudioManager mAudioManager;

    private String mLocalApkPath = null;
    private View mImgProgram, mImgFrequency, mImgMyRife;
    private LinearLayout img_my_shop;
    private long mReferenceId = 0;

    private View mViewDisable;
    private Button mBtnCustom,btnShop;
    private RelativeLayout banner_wrapper,flash_sale;
    private CallbackManager mCallbackManager;
    private CountDownTimer mCountDownTimer;
    private TextView mTvHours, mTvMinutes, mTvSeconds;
    private CustomFontTextView tv_title;
    private FirebaseAnalytics mFirebaseAnalytics;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isRequestAudioFocus) { requestToSilentOtherMusicApps(); }
        }
    };

    private BroadcastReceiver mBroadcastReceiverPurchase = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) { updateView(); }
    };

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (mReferenceId == referenceId) {
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainMenuActivity.this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("[New APK] " + getString(R.string.app_name))
                        .setContentText("Download completed");
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.notify(455, mBuilder.build());
                }

                autoInstallNewAPK();
            }
        }
    };

    @Override
    protected int initLayout() { return R.layout.activity_main_menu; }

    @Override
    protected void initComponents() {

        //todo Facebook? delete
        mCallbackManager = CallbackManager.Factory.create();
        try {
            LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) { }

                @Override
                public void onCancel() { }

                @Override
                public void onError(FacebookException exception) { }
            });
        } catch (RuntimeException ignored) { }

        requestPermissions();

        registerReceiver(mBroadcastReceiverPurchase, new IntentFilter(Constants.BROADCAST_ACTION_PURCHASED));

        setTitle(getString(R.string.app_name));


        InstallReferrerClient referrerClient;

        referrerClient = InstallReferrerClient.newBuilder(this).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                switch (responseCode) {
                    case InstallReferrerClient.InstallReferrerResponse.OK:
                        // Connection established.
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        // API not available on the current Play Store app.
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        // Connection couldn't be established.
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

        AppsFlyerLib.getInstance().init("aNPCN6auSrzidSGCeMrg9R", null, this);
        AppsFlyerLib.getInstance().start(this);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        tv_title = findViewById(R.id.tv_title);

        String user_name = SharedPreferenceHelper.getInstance(MainMenuActivity.this).get("user_name");
        tv_title.setText("RifeApp" + " - " + "Welcome, " + user_name);
        mTvHours = findViewById(R.id.flash_sale_hours);
        mTvMinutes = findViewById(R.id.flash_sale_minutes);
        mTvSeconds = findViewById(R.id.flash_sale_seconds);

        mImgProgram = findViewById(R.id.img_program);
        mImgFrequency = findViewById(R.id.img_frequency);
        img_my_shop =  findViewById(R.id.img_my_shop);
        mImgMyRife = findViewById(R.id.img_my_rife);
        mViewDisable = findViewById(R.id.view_disable_custom);
        mBtnCustom = findViewById(R.id.btn_custom);
        btnShop = findViewById(R.id.btn_shop);
        banner_wrapper = findViewById(R.id.banner_wrapper);
        flash_sale = findViewById(R.id.flash_sale);

        initFlashSale();
        initRightMenu();

        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", something);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }

        banner_wrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.Meditation.Sounds.frequencies&hl=en")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.Meditation.Sounds.frequencies&hl=en")));
                }
            }
        });

        btnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://qilifestore.com/collections/rife-machine-frequency-systems?ref=rifeappa"));
                startActivity(browserIntent);
            }
        });

        img_my_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://qilifestore.com/collections/rife-machine-frequency-systems?ref=rifeappa"));
                startActivity(browserIntent);
            }
        });

        flash_sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSubscriptionAppDialog();
            }
        });

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void initFlashSale() {
        long flashSaleRemainTime = Utilities.getFlaseSaleRemainTime(this);

        if(flashSaleRemainTime > 0){
           flash_sale.setVisibility(View.VISIBLE);
           setCountdownTimer(flashSaleRemainTime);
        } else {
            flash_sale.setVisibility(View.GONE);
        }
    }

    public void setCountdownTimer(long totalTime) {
        mCountDownTimer = new CountDownTimer(totalTime, 1000) {
            @Override
            public void onTick(long l) {
                int totalSeconds = (int)(l / 1000);
                int days = totalSeconds/(24*3600);
                int remainder = totalSeconds - (days*24*3600);
                int hours = remainder / 3600;
                remainder = remainder - (hours * 3600);
                int mins = remainder / 60;
                remainder = remainder - mins * 60;
                int secs = remainder;

                mTvHours.setText(hours > 9 ? "" + hours : "0" + hours);
                mTvMinutes.setText(mins > 9 ? "" + mins : "0" + mins);
                mTvSeconds.setText(secs > 9 ? "" + secs : "0" + secs);
            }

            @Override
            public void onFinish() {
                initFlashSale();
            }
        };
        mCountDownTimer.start();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPreferenceHelper.getInstance(this).getLong(Constants.EXTRA_FIRST_INSTALLER_APP_TIME) == 0L) {
            Calendar calendar = Calendar.getInstance();
            SharedPreferenceHelper.getInstance(this).setLong(Constants.EXTRA_FIRST_INSTALLER_APP_TIME, calendar.getTimeInMillis());
        }

        if (!BuildConfig.IS_FREE) {
            new GetFlashSaleTask(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        //Turn on/off music from another play controller
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.BROADCAST_PLAY_PAUSE_CONTROLLER_GLOBAL));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateView();
        initFlashSale();
        ourMainRunning = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
        unregisterReceiver(mBroadcastReceiverPurchase);
        unregisterReceiver(mBroadcastReceiver);
        ourMainRunning = false;
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    public void requestToSilentOtherMusicApps() {
        isRequestAudioFocus = false;
        mAudioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {
                switch (i) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            isRequestAudioFocus = false;   // Resume your media player here
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            sendBroadcast(new Intent("PauseFreq"));
                            isRequestAudioFocus = true;   // Pause your media player here
                            break;
                }
            }
            }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    private void checkPermissionsResult() {
        if (hasPermissions(this, PERMISSIONS)) {
            onPermissionsGranted();
        }
    }

    private void onPermissionsGranted() {
        if (!BuildConfig.IS_FREE) {
            checkAndShowFlashSale();
        } else {
            new GetAPKNewVersionTask(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void requestPermissions() {
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } else {
            onPermissionsGranted();
        }
    }

    public void checkAndShowFlashSale() {
        if (getIntent().hasExtra(Constants.EXTRA_FLASH_SALE_TYPE)) {
            if (!SharedPreferenceHelper.getInstance(this).getBool(Constants.KEY_PURCHASED)) {
                int type = getIntent().getIntExtra(Constants.EXTRA_FLASH_SALE_TYPE, 0);
                if (type > 0 && type != Constants.EXTRA_REMINDER_NOTIFICATION) {
                    startSubscriptionActivity();
                }
            }
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) { checkPermissionsResult(); }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void updateView() {
        mViewDisable.setVisibility(View.GONE);
        mBtnCustom.setText(getString(R.string.txt_custom));
        mBtnCustom.setTextColor(Color.parseColor("#FFFFFF"));
        mImgMyRife.setEnabled(true);
        mImgMyRife.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainMenuActivity.this, MainActivity.class);
                i.putExtra(IN_APP, 3);
                startActivity(i);
            }
        });

        if (!BuildConfig.IS_FREE) {
            if (!SharedPreferenceHelper.getInstance(this).getBool(Constants.KEY_PURCHASED)) {
                mViewDisable.setVisibility(View.VISIBLE);
                banner_wrapper.setVisibility(View.VISIBLE);
                mBtnCustom.setText(getString(R.string.txt_custom));
                mBtnCustom.setTextColor(Color.parseColor("#707070"));
                mImgMyRife.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSubscriptionAppDialog();
                    }
                });
            }
            else
                banner_wrapper.setVisibility(View.GONE);
        }
    }

    @Override
    protected void addListener() {
        hiddenNavLeft();
        showNavRight(R.drawable.ic_menu, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMenuRight();
            }
        });

        mImgProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainMenuActivity.this, MainActivity.class);
                i.putExtra(IN_APP, 1);
                startActivity(i);
            }
        });
        mImgFrequency.performClick();
        mImgFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainMenuActivity.this, MainActivity.class);
                i.putExtra(IN_APP, 2);
                startActivity(i);
            }
        });
    }

    @Override
    public void onConnectionOpen(BaseTask task) { }

    @Override
    public void onConnectionSuccess(BaseTask task, Object data) {
        //flash sale task
        if (task instanceof GetFlashSaleTask) {
            String jsonFlashSale = (String) data;
            if (jsonFlashSale != null && jsonFlashSale.length() > 0) {
                GetFlashSaleOutput jsonCurrent = new Gson().fromJson(jsonFlashSale, GetFlashSaleOutput.class);

                String flashSaleString = "";
                if (jsonCurrent != null && jsonCurrent.flashSale != null) {
                    flashSaleString = new Gson().toJson(jsonCurrent.flashSale);
                }
                String jsonString = SharedPreferenceHelper.getInstance(this).get(Constants.PREF_FLASH_SALE);
                String flashSaleOrgString = "";
                if (jsonString != null) {
                    GetFlashSaleOutput jsonOrg = new Gson().fromJson(jsonString, GetFlashSaleOutput.class);
                    if (jsonOrg != null && jsonOrg.flashSale != null) {
                        flashSaleOrgString = new Gson().toJson(jsonOrg.flashSale);
                    }
                }

                SharedPreferenceHelper.getInstance(this).set(Constants.PREF_FLASH_SALE, jsonFlashSale);

                if (!flashSaleString.equalsIgnoreCase(flashSaleOrgString)) {
                    SharedPreferenceHelper.getInstance(this).setInt(Constants.PREF_FLASH_SALE_COUNTERED, 0);
                }

                if (jsonCurrent != null) {
                    if (SharedPreferenceHelper.getInstance(this).getInt(Constants.PREF_FLASH_SALE_COUNTERED) <= jsonCurrent.flashSale.getProposalsCount()) {
                        QcAlarmManager.createAlarms(this);
                    } else {
                        QcAlarmManager.clearAlarms(this);
                    }
                }

                //Create reminder
                QcAlarmManager.createReminderAlarm(this);
            } else {
                QcAlarmManager.clearAlarms(this);
            }
        }

        // new version task
        if (task instanceof GetAPKNewVersionTask) {
            GetAPKsNewVersionOutput output = (GetAPKsNewVersionOutput) data;
            if (output.code == 200) {
                if (output.apks != null && output.apks.size() > 0) {
                    String currentVer = BuildConfig.VERSION_NAME;
                    String apkUrl = output.apks.get(0);
                    String[] pathSplit = apkUrl.split("/");
                    if (pathSplit.length > 0) {
                        String fileName = pathSplit[pathSplit.length - 1];
                        String newVersion = fileName.replace("Rift_v", "").replace(".apk", "");
                        String[] newVs = newVersion.split("\\.");
                        String[] currentVs = currentVer.split("\\.");

                        if (newVs.length == 3 && currentVs.length == 3) {
                            if ((Integer.parseInt(newVs[0]) * 100 + Integer.parseInt(newVs[1]) * 10 + Integer.parseInt(newVs[2]) > Integer.parseInt(currentVs[0]) * 100 + Integer.parseInt(currentVs[1]) * 10 + Integer.parseInt(currentVs[2]))) {
                                dialogConfirmUpdateApk(apkUrl, fileName);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onConnectionError(BaseTask task, Exception exception) { }

    public void dialogConfirmUpdateApk(final String apkUrl, final String fileName) {
        try {
            CustomDialogMessageConfirm messageConfirm = new CustomDialogMessageConfirm(MainMenuActivity.this);
            messageConfirm.show();
            messageConfirm.setTextButtonLeft(getString(R.string.txt_disagree));
            messageConfirm.setTextButtonRight(getString(R.string.txt_agree));
            messageConfirm.setTextTitle(getString(R.string.txt_warning_update_newversion_title));
            messageConfirm.setTextContent(getString(R.string.txt_warning_update_newversion));

            messageConfirm.setOnClickConfirmListener(new CustomDialogMessageConfirm.OnClickConfirmListener() {
                @Override
                public void onClick(String value) {
                    downloadAPK(apkUrl, fileName);
                }
            });
        } catch (WindowManager.BadTokenException ignored) { }
    }

    public void downloadAPK(String apkUrl, String fileName) {
        File[] listFiles = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).listFiles();
        if (listFiles != null) {
            for (File f : listFiles) { f.delete(); }
        }

        File apkFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + fileName + ".apk");
        mLocalApkPath = apkFile.getPath();
        Toast.makeText(this, getString(R.string.txt_downloading_dot), Toast.LENGTH_LONG).show();
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri download_Uri = Uri.parse(apkUrl);
        DownloadManager.Request request = new DownloadManager.Request(download_Uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle("[New APK] " + getString(R.string.app_name));
        request.setDescription("Downloading " + fileName);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationUri(Uri.fromFile(apkFile));
        if (downloadManager != null) { mReferenceId = downloadManager.enqueue(request); }
    }

    public void autoInstallNewAPK() {
        if (mLocalApkPath != null) {
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(FileProvider.getUriForFile(this, "com.zappkit.zappid.provider", new File(mLocalApkPath)));
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(mLocalApkPath)), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            startActivity(intent);
        }
    }
}
