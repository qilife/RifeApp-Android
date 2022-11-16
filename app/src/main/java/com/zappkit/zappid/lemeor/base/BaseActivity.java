package com.zappkit.zappid.lemeor.base;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zappkit.zappid.BuildConfig;
import com.zappkit.zappid.LoginActivity;
import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.lemeor.MainMenuActivity;
import com.zappkit.zappid.lemeor.top_menu.InstructionsActivity;
import com.zappkit.zappid.lemeor.subscription.SubscribeActivity;
import com.zappkit.zappid.lemeor.subscription.SubscriptionInAppActivity;
import com.zappkit.zappid.MenuAdapter;
import com.zappkit.zappid.MenuModel;
import com.zappkit.zappid.lemeor.main_menu.player.FrequencyUIActivity;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.models.SequenceListModel;
import com.zappkit.zappid.lemeor.top_menu.SettingActivity;
import com.zappkit.zappid.utilbilling.IabBroadcastReceiver;
import com.zappkit.zappid.utilbilling.IabHelper;
import com.zappkit.zappid.utilbilling.IabResult;
import com.zappkit.zappid.utilbilling.Inventory;
import com.zappkit.zappid.utilbilling.Purchase;
import com.zappkit.zappid.utilbilling.SkuDetails;
import com.zappkit.zappid.lemeor.tools.Constants;
import com.zappkit.zappid.lemeor.tools.QcAlarmManager;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import com.zappkit.zappid.lemeor.tools.Utilities;
import com.zappkit.zappid.views.CustomDialogMessageConfirm;
import com.zappkit.zappid.views.CustomDialogPaymentApp;
import com.zappkit.zappid.views.DialogSubscriptionApp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity implements DrawerLayout.DrawerListener, IabBroadcastReceiver.IabBroadcastListener {
    private boolean isOnPause = false;
    public ImageView mImvNavRight;
    private ImageView mImvNavLeft;
    private TextView mTvTitle;
    protected ProgressDialog mProgressDialog;
    protected DrawerLayout mDrawerLayout;
    private View mLayoutSlideMenu;
    private List<MenuModel> mMenuLists = new ArrayList<>();
    protected DbHelper mDatabase;

    private BroadcastReceiver mBroadcastReceiverPurchase = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SharedPreferenceHelper.getInstance(BaseActivity.this).getBool(Constants.KEY_PURCHASED)) {
                if (mCustomDialogPaymentApp != null && mCustomDialogPaymentApp.isShowing()) {
                    mCustomDialogPaymentApp.dismiss();
                }
                if (BaseActivity.this instanceof SubscribeActivity
                        || BaseActivity.this instanceof SubscriptionInAppActivity) { finish(); }
            }
        }
    };

    protected CustomDialogPaymentApp mCustomDialogPaymentApp;
    private DialogSubscriptionApp mDialogSubscriptionApp;

    private Handler mHandlerCountDown = new Handler();
    private Runnable mRunnableCountDown = new Runnable() {
        @Override
        public void run() {
            SharedPreferenceHelper.getInstance(BaseActivity.this).setLong(Constants.FRE_PLAYER_FOR_FREE_TIME_5_MINTS, 0);
            if (mCustomDialogPaymentApp != null && mCustomDialogPaymentApp.isShowing()) {
                mCustomDialogPaymentApp.dismiss();
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiverInAppDialog = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isOnPause) {
                if (mCustomDialogPaymentApp != null && mCustomDialogPaymentApp.isShowing()) {
                    mCustomDialogPaymentApp.dismiss();
                }
                showPurchaseDialog();
                checkLimitTimeForFree();
            }
        }
    };


    //in-app
    // Will the subscription auto-renew?
    public boolean mAutoRenewEnabled = false;

    // Tracks the currently owned subscription, and the options in the Manage dialog
    public String mDelaroySku = "";
    // (arbitrary) request code for the purchase flow
    public static final int RC_REQUEST = 10001;
    // The helper object
    public IabHelper mHelper;
    // Provides purchase notification while this app is running
    public IabBroadcastReceiver mBroadcastReceiver;


    protected abstract int initLayout();

    protected abstract void initComponents();

    protected abstract void addListener();

    private BroadcastReceiver broadcastReceiverFlashSaleNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context p0, Intent intent) {
            if (intent.hasExtra(Constants.EXTRA_FLASH_SALE_TYPE)) {
                int type = intent.getIntExtra(Constants.EXTRA_FLASH_SALE_TYPE, 0);
                if (!SharedPreferenceHelper.getInstance(p0).getBool(Constants.KEY_PURCHASED)) {
                    if (type > 0 && type != Constants.EXTRA_REMINDER_NOTIFICATION) {
                        dismissAllPaymentAppDialog();
                        if (BaseActivity.this instanceof MainMenuActivity) {
                            startSubscriptionActivity();
                        }
                    }
                }
                if (type == Constants.EXTRA_REMINDER_NOTIFICATION) {
                    if (BaseActivity.this instanceof MainMenuActivity) {
                        Intent intentMain = new Intent(BaseActivity.this, MainMenuActivity.class);
                        intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intentMain.putExtra(Constants.EXTRA_FLASH_SALE_TYPE, type);
                        startActivity(intentMain);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = initLayout();
        if (layoutId != 0) {
            setContentView(layoutId);
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.txt_waiting));

        if (!SharedPreferenceHelper.getSharedPreferences(this).getBoolean("isClearOutForNewVersion", false)) {
            SharedPreferenceHelper.getSharedPreferences(this).edit().putBoolean("isClearOutForNewVersion", true).apply();
            if (getDatabasePath(DbHelper.getDbHelperName()).exists()) {
                backupUserData();
            } else {
                mDatabase = new DbHelper(BaseActivity.this);
            }
        } else {
            mDatabase = new DbHelper(BaseActivity.this);
        }

        initNavigation();
        initComponents();
        addListener();

        if (!BuildConfig.IS_FREE) { initHelper(); }

        registerReceiver(mBroadcastReceiverInAppDialog, new IntentFilter(Constants.BROADCAST_SHOW_DIALOG_IN_APP));
        registerReceiver(mBroadcastReceiverPurchase, new IntentFilter(Constants.BROADCAST_ACTION_PURCHASED));
        registerReceiver(broadcastReceiverFlashSaleNotification, new IntentFilter(Constants.ACTION_RECEIVE_FLASH_SALE_NOTIFICATION));
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnPause = false;
        checkLimitTimeForFree();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnPause = true;
    }

    public void startSubscriptionActivity() {
        Intent intent = new Intent(this, SubscribeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void showPurchaseDialog() {
        if (mCustomDialogPaymentApp != null && mCustomDialogPaymentApp.isShowing()) {
            mCustomDialogPaymentApp.dismiss();
        }
        long flashSaleRemainTime = Utilities.getFlaseSaleRemainTime(this);
        if (flashSaleRemainTime > 0) {
            startSubscriptionActivity();
        } else {
            mCustomDialogPaymentApp = new CustomDialogPaymentApp(BaseActivity.this);
            mCustomDialogPaymentApp.show();
            mCustomDialogPaymentApp.setCountdownTimer();
            mCustomDialogPaymentApp.setOnClickCancelListener(new CustomDialogPaymentApp.OnClickCancelListener() {
                @Override
                public void onClick() {
                    Intent intent = new Intent(BaseActivity.this, SubscribeActivity.class);
                    intent.putExtra(Constants.ENUM_FREE, true);
                    BaseActivity.this.startActivity(intent);
                    mCustomDialogPaymentApp.dismiss();
                }
            });
        }
    }

    public void showSubscriptionAppDialog() {
        if (mDialogSubscriptionApp != null && mDialogSubscriptionApp.isShowing()) {
            mDialogSubscriptionApp.dismiss();
        }
        long flashSaleRemainTime = Utilities.getFlaseSaleRemainTime(this);
        if (flashSaleRemainTime > 0) {
            startSubscriptionActivity();
        } else {
            mDialogSubscriptionApp = new DialogSubscriptionApp(this);
            mDialogSubscriptionApp.show();
        }
    }

    public void dismissAllPaymentAppDialog() {
        if (mCustomDialogPaymentApp != null && mCustomDialogPaymentApp.isShowing()) {
            mCustomDialogPaymentApp.dismiss();
        }
        if (mDialogSubscriptionApp != null && mDialogSubscriptionApp.isShowing()) {
            mDialogSubscriptionApp.dismiss();
        }
    }

    public void checkLimitTimeForFree() {
        long currentTotalTime = SharedPreferenceHelper.getInstance(this).getLong(Constants.FRE_PLAYER_FOR_FREE_TIME_5_MINTS);
        if (currentTotalTime >= Constants.LIMIT_TIME_5_MINUTES) {
            long limitTime = Calendar.getInstance().getTimeInMillis() - SharedPreferenceHelper.getInstance(this).getLong(Constants.PRE_LIMIT_TIME_PLAYER);
            if (limitTime < Constants.ONE_HOUR_TIME) {
                mHandlerCountDown.postDelayed(mRunnableCountDown, Constants.ONE_HOUR_TIME - limitTime);
            } else {
                SharedPreferenceHelper.getInstance(this).setLong(Constants.FRE_PLAYER_FOR_FREE_TIME_5_MINTS, 0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mBroadcastReceiver != null){
                unregisterReceiver(mBroadcastReceiver);
            }
            unregisterReceiver(mBroadcastReceiverInAppDialog);
            unregisterReceiver(broadcastReceiverFlashSaleNotification);
            unregisterReceiver(mBroadcastReceiverPurchase);
        } catch (IllegalArgumentException ignored) { }
        mHandlerCountDown.removeCallbacksAndMessages(null);
    }

    public void initRightMenu() {
        if (mDrawerLayout != null) {
            mDrawerLayout.addDrawerListener(this);
            loadMenu();
        }
    }

    private void initHelper() {
        // Create the helper, passing it our context and the public key to verify signatures with
        mHelper = new IabHelper(this, Constants.base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
//                    complain("Problem setting up in-app billing: " + result);
                    complain("In App Set UP error:: Please check gmail account settings/ Credit Card Info etc");
                    return;
                }
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
                mBroadcastReceiver = new IabBroadcastReceiver(BaseActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
//                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    ArrayList<String> skuList = new ArrayList<>();
                    skuList.add(Constants.SKU_RIFE_FREE);
                    skuList.add(Constants.SKU_RIFE_MONTHLY);
                    skuList.add(Constants.SKU_RIFE_YEARLY);
                    skuList.add(Constants.SKU_RIFE_FREE_FLASH_SALE);
                    skuList.add(Constants.SKU_RIFE_MONTHLY_FLASH_SALE);
                    skuList.add(Constants.SKU_RIFE_YEARLY_FLASH_SALE);
                    mHelper.queryInventoryAsync(true, null, skuList, mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });
    }

    private void loadMenu() {
        RecyclerView rcMenu = findViewById(R.id.recyclerview_menu);
        rcMenu.setLayoutManager(new LinearLayoutManager(this));
        mMenuLists.clear();
        mMenuLists.add(new MenuModel(getString(R.string.action_player), MenuModel.MENU_TYPE.MENU_OPEN_PLAYER));
        mMenuLists.add(new MenuModel(getString(R.string.txt_instructions), MenuModel.MENU_TYPE.MENU_INSTRUCTION));
        mMenuLists.add(new MenuModel(getString(R.string.action_settings), MenuModel.MENU_TYPE.MENU_SETTINGS));
        mMenuLists.add(new MenuModel(getString(R.string.action_about), MenuModel.MENU_TYPE.MENU_ABOUT));
        mMenuLists.add(new MenuModel(getString(R.string.action_logout), MenuModel.MENU_TYPE.MENU_LOGOUT));

        MenuAdapter menuAdapter = new MenuAdapter(this, mMenuLists);
        rcMenu.setAdapter(menuAdapter);
        menuAdapter.setIOnClickItemListener(new MenuAdapter.IOnClickItemListener() {
            @Override
            public void onItemClick(MenuModel.MENU_TYPE menuType) {
                Intent i = new Intent();
                switch (menuType) {
                    case MENU_OPEN_PLAYER:
                        if (!BaseActivity.this.mDatabase.getNextFreqOnPlayList().equals("done!")) {
                            i.setClass(BaseActivity.this, FrequencyUIActivity.class);
                            startActivity(i);
                            break;
                        }
                        Toast.makeText(BaseActivity.this, getResources().getString(R.string.inactivePlayer), Toast.LENGTH_LONG).show();
                        break;
                    case MENU_INSTRUCTION:
                        startActivity(new Intent(BaseActivity.this, InstructionsActivity.class));
                        break;
                    case MENU_SETTINGS:
                        startActivity(new Intent(BaseActivity.this, SettingActivity.class));
                        break;
                    case MENU_ABOUT:
                        try {
                            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                            CustomDialogMessageConfirm dialog = new CustomDialogMessageConfirm(BaseActivity.this);
                            dialog.show();
                            dialog.setData(getString(R.string.AboutDialogTitle), getResources().getString(R.string.AboutDialogCompany), "Version: " + versionName);
                            break;
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                            break;
                        }
                    case MENU_LOGOUT:
                        SharedPreferenceHelper.getInstance(BaseActivity.this).setBool("islogin",false);
                        startActivity(new Intent(BaseActivity.this, LoginActivity.class));
                        finish();
                        break;
                }
                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawer(mLayoutSlideMenu);
                }
            }
        });
    }

    public void toggleMenuRight() {
        if (mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(mLayoutSlideMenu)) {
                mDrawerLayout.closeDrawer(mLayoutSlideMenu);
            } else {
                mDrawerLayout.openDrawer(mLayoutSlideMenu);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void initNavigation() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mLayoutSlideMenu = findViewById(R.id.layout_left_menu);

        mTvTitle = findViewById(R.id.tv_title);
        if (mTvTitle != null) {
            mImvNavLeft = findViewById(R.id.imv_left);
            mImvNavRight = findViewById(R.id.imv_right);
            mTvTitle.setSelected(true);
        }
    }

    public void showNavigation(ImageView imageView, int resId, View.OnClickListener listener) {
        if (imageView != null) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(resId);
            if (listener != null) {
                imageView.setOnClickListener(listener);
            }
        }
    }

    public void showNavLeft(int resId, View.OnClickListener listener) {
        showNavigation(mImvNavLeft, resId, listener);
    }

    public void showNavRight(int resId, View.OnClickListener listener) {
        showNavigation(mImvNavRight, resId, listener);
    }

    public void hiddenNavRight() {
        mImvNavRight.setVisibility(View.INVISIBLE);
    }

    public void hiddenNavLeft() {
        mImvNavLeft.setVisibility(View.INVISIBLE);
    }

    public void setTitle(String title) {
        if (mTvTitle != null) {
            mTvTitle.setText(title);
        }
    }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void toast(int messageId) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void finish() {
        hideKeyBoard();
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void hideKeyBoard() {
        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        InputMethodManager inputManager = (InputMethodManager) BaseActivity.this
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(
                                BaseActivity.this.getCurrentFocus().getApplicationWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    } catch (IllegalStateException e) {
                    } catch (Exception e) {
                    }
                }
            });

        } catch (IllegalStateException e) {
            // TODO: handle exception
        } catch (Exception e) {
        }
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            SkuDetails detail7Day = inventory.getSkuDetails(Constants.SKU_RIFE_FREE);
            SkuDetails detailsMonth = inventory.getSkuDetails(Constants.SKU_RIFE_MONTHLY);
            SkuDetails detailsYear = inventory.getSkuDetails(Constants.SKU_RIFE_YEARLY);
            SkuDetails detail7DayFlashsale = inventory.getSkuDetails(Constants.SKU_RIFE_FREE_FLASH_SALE);
            SkuDetails detailsMonthFlashsale = inventory.getSkuDetails(Constants.SKU_RIFE_MONTHLY_FLASH_SALE);
            SkuDetails detailsYearFlashsale = inventory.getSkuDetails(Constants.SKU_RIFE_YEARLY_FLASH_SALE);

            if (detail7Day != null) {
                SharedPreferenceHelper.getInstance(getApplicationContext()).setPriceByCurrency(Constants.PRICE_7_DAY_TRIAL, detail7Day.getPrice());
            }
            if (detailsMonth != null) {
                SharedPreferenceHelper.getInstance(getApplicationContext()).setPriceByCurrency(Constants.PRICE_1_MONTH, detailsMonth.getPrice());
            }
            if (detailsYear != null) {
                SharedPreferenceHelper.getInstance(getApplicationContext()).setPriceByCurrency(Constants.PRICE_1_YEAR, detailsYear.getPrice());
            }
            if (detail7DayFlashsale != null) {
                SharedPreferenceHelper.getInstance(getApplicationContext()).setPriceByCurrency(Constants.PRICE_7_DAY_TRIAL_FLASH_SALE, detail7DayFlashsale.getPrice());
            }
            if (detailsMonthFlashsale != null) {
                SharedPreferenceHelper.getInstance(getApplicationContext()).setPriceByCurrency(Constants.PRICE_1_MONTH_FLASH_SALE, detailsMonthFlashsale.getPrice());
            }
            if (detailsYearFlashsale != null) {
                SharedPreferenceHelper.getInstance(getApplicationContext()).setPriceByCurrency(Constants.PRICE_1_YEAR_FLASH_SALE, detailsYearFlashsale.getPrice());
            }

            // First find out which subscription is auto renewing
            Purchase rifeSkuFree = inventory.getPurchase(Constants.SKU_RIFE_FREE);
            Purchase rifeSkuMonth = inventory.getPurchase(Constants.SKU_RIFE_MONTHLY);
            Purchase rifeSkuYear = inventory.getPurchase(Constants.SKU_RIFE_YEARLY);

            Purchase rifeSkuFreeFlashsale = inventory.getPurchase(Constants.SKU_RIFE_FREE_FLASH_SALE);
            Purchase rifeSkuMonthFlashsale = inventory.getPurchase(Constants.SKU_RIFE_MONTHLY_FLASH_SALE);
            Purchase rifeSkuYearFlashsale = inventory.getPurchase(Constants.SKU_RIFE_YEARLY_FLASH_SALE);

            if ((rifeSkuFree != null && rifeSkuFree.isAutoRenewing()) || (rifeSkuFreeFlashsale != null && rifeSkuFreeFlashsale.isAutoRenewing())) {
                mDelaroySku = Constants.SKU_RIFE_FREE;
                mAutoRenewEnabled = true;
                SharedPreferenceHelper.getInstance(BaseActivity.this).setBool(Constants.KEY_PURCHASED, true);
                Intent intent = new Intent(Constants.BROADCAST_ACTION_PURCHASED);
                sendBroadcast(intent);

                QcAlarmManager.clearAlarms(BaseActivity.this);
            } else if ((rifeSkuMonth != null && rifeSkuMonth.isAutoRenewing()) || (rifeSkuMonthFlashsale != null && rifeSkuMonthFlashsale.isAutoRenewing())) {
                mDelaroySku = Constants.SKU_RIFE_MONTHLY;
                mAutoRenewEnabled = true;
                SharedPreferenceHelper.getInstance(BaseActivity.this).setBool(Constants.KEY_PURCHASED, true);
                Intent intent = new Intent(Constants.BROADCAST_ACTION_PURCHASED);
                sendBroadcast(intent);

                QcAlarmManager.clearAlarms(BaseActivity.this);
            } else if ((rifeSkuYear != null && rifeSkuYear.isAutoRenewing()) || (rifeSkuYearFlashsale != null && rifeSkuYearFlashsale.isAutoRenewing())) {
                mDelaroySku = Constants.SKU_RIFE_YEARLY;
                mAutoRenewEnabled = true;
                SharedPreferenceHelper.getInstance(BaseActivity.this).setBool(Constants.KEY_PURCHASED, true);
                Intent intent = new Intent(Constants.BROADCAST_ACTION_PURCHASED);
                sendBroadcast(intent);

                QcAlarmManager.clearAlarms(BaseActivity.this);
            } else {
                mDelaroySku = "";
                mAutoRenewEnabled = false;
                SharedPreferenceHelper.getInstance(BaseActivity.this).setBool(Constants.KEY_PURCHASED, false);
            }
        }
    };

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        try {
            ArrayList<String> skuList = new ArrayList<String>();
            skuList.add(Constants.SKU_RIFE_FREE);
            skuList.add(Constants.SKU_RIFE_MONTHLY);
            skuList.add(Constants.SKU_RIFE_YEARLY);
            skuList.add(Constants.SKU_RIFE_FREE_FLASH_SALE);
            skuList.add(Constants.SKU_RIFE_MONTHLY_FLASH_SALE);
            skuList.add(Constants.SKU_RIFE_YEARLY_FLASH_SALE);
            mHelper.queryInventoryAsync(true, null, skuList, mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    // "Subscribe to delaroy" button clicked. Explain to user, then start purchase
    // flow for subscription.
    public void onPurchaseProduct(String skuRife) {
        try {
            mHelper.launchSubscriptionPurchaseFlow(this, skuRife, RC_REQUEST, mPurchaseFinishedListener, "");
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");
        } catch (IllegalStateException ignored){ }
        // Reset the dialog options
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
        }
    }

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();


        return true;
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                return;
            }

            if (purchase.getSku().equals(Constants.SKU_RIFE_FREE)
                    || purchase.getSku().equals(Constants.SKU_RIFE_MONTHLY)
                    || purchase.getSku().equals(Constants.SKU_RIFE_YEARLY)
                    || purchase.getSku().equals(Constants.SKU_RIFE_FREE_FLASH_SALE)
                    || purchase.getSku().equals(Constants.SKU_RIFE_MONTHLY_FLASH_SALE)
                    || purchase.getSku().equals(Constants.SKU_RIFE_YEARLY_FLASH_SALE)) {

                mAutoRenewEnabled = purchase.isAutoRenewing();
                mDelaroySku = purchase.getSku();
                SharedPreferenceHelper.getInstance(BaseActivity.this).setBool(Constants.KEY_PURCHASED, true);
                Intent intent = new Intent(Constants.BROADCAST_ACTION_PURCHASED);
                sendBroadcast(intent);

                QcAlarmManager.clearAlarms(BaseActivity.this);
            }
        }
    };

    void complain(String message) {
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        if (!isFinishing()) {
            bld.create().show();
        }
    }

    private void backupUserData() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.creating_data));
        progressDialog.setCancelable(false);
        progressDialog.show();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> myFrequencies = new ArrayList<>();
                ArrayList<SequenceListModel> mySequences = new ArrayList<>();
                ArrayList<SequenceListModel> myPlaylists = new ArrayList<>();
                BaseActivity.this.mDatabase = new DbHelper(BaseActivity.this);
                Cursor cursor = BaseActivity.this.mDatabase.getMyFrequencies();
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        myFrequencies.add(cursor.getString(cursor.getColumnIndex("frequency")));
                    } while (cursor.moveToNext());
                }
                cursor.close();
                cursor = BaseActivity.this.mDatabase.getMySequences();
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        SequenceListModel tempModel = new SequenceListModel();
                        tempModel.setSequenceTitle(cursor.getString(cursor.getColumnIndex("name")));
                        tempModel.setFrequecyList(cursor.getString(cursor.getColumnIndex("list")));
                        tempModel.setNotes(cursor.getString(cursor.getColumnIndex("description")));
                        mySequences.add(tempModel);
                    } while (cursor.moveToNext());
                }
                cursor.close();
                cursor = BaseActivity.this.mDatabase.getMyPlaylists();
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        SequenceListModel tempModel = new SequenceListModel();
                        tempModel.setSequenceTitle(cursor.getString(cursor.getColumnIndex("name")));
                        tempModel.setFrequecyList(cursor.getString(cursor.getColumnIndex("list")));
                        tempModel.setNotes(cursor.getString(cursor.getColumnIndex("description")));
                        myPlaylists.add(tempModel);
                    } while (cursor.moveToNext());
                }
                cursor.close();
                BaseActivity.this.mDatabase.close();
                deleteDatabase(BaseActivity.this.mDatabase.getDatabaseName());
                BaseActivity.this.mDatabase = new DbHelper(BaseActivity.this);
                for (String sequences : myFrequencies) {
                    BaseActivity.this.mDatabase.insertMyFrequency(sequences);
                }
                for (SequenceListModel sequences : mySequences) {
                    BaseActivity.this.mDatabase.insertMySequence(sequences.getSequenceTitle(), sequences.getNotes(), sequences.getFrequencyList());
                }
                for (SequenceListModel sequences : myPlaylists) {
                    BaseActivity.this.mDatabase.insertMyPlaylist(sequences.getSequenceTitle(), sequences.getNotes(), sequences.getFrequencyList());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }
}
