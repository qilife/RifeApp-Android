package com.zappkit.zappid.lemeor.subscription;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.base.BaseActivity;
import com.zappkit.zappid.lemeor.tools.Utilities;
import com.zappkit.zappid.lemeor.tools.Constants;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;

public class SubscribeActivity extends BaseActivity {
    private View mBtnFreeTrial, mBtnMonth, mBtnYear;
    private View mGroupSubscription, mGroupFlashSale;
    private TextView mTvPriceFree;
    private TextView mTvPriceMonth;
    private TextView mTvPriceYear;
    private TextView mTvDayFlashSale, mTvDays, mTvHours, mTvMinutes, mTvSeconds;
    private CountDownTimer mCountDownTimer;

    @Override
    protected int initLayout() {
        return R.layout.activity_subscribe;
    }

    @Override
    protected void initComponents() {
        setTitle(getString(R.string.txt_subcrise));
        mBtnFreeTrial = findViewById(R.id.btn_free);
        mBtnMonth = findViewById(R.id.btn_month);
        mBtnYear = findViewById(R.id.btn_year);
        mGroupSubscription = findViewById(R.id.groupSubscriptions);
        mGroupFlashSale = findViewById(R.id.groupFlashSale);
        mTvDayFlashSale = findViewById(R.id.tvDayFlashSale);
        mTvDays = findViewById(R.id.tvDays);
        mTvHours = findViewById(R.id.tvHours);
        mTvMinutes = findViewById(R.id.tvMinutes);
        mTvSeconds = findViewById(R.id.tvSeconds);

        mTvPriceFree = findViewById(R.id.tv_price_7_day);
        mTvPriceMonth = findViewById(R.id.tv_price_month);
        mTvPriceYear = findViewById(R.id.tv_price_year);

        mTvDayFlashSale = findViewById(R.id.tvDayFlashSale);


        boolean intent = getIntent().getBooleanExtra(Constants.ENUM_FREE, false);
        if (intent) {
            Intent intent1 = new Intent(SubscribeActivity.this, SubscriptionInAppActivity.class);
            intent1.putExtra(Constants.IN_APP_TYPE, Constants.ENUM_FREE);
            startActivity(intent1);
        }

        initFlashSale();
    }

    public void initFlashSale(){
        long flashSaleRemainTime = Utilities.getFlaseSaleRemainTime(this);

        if(flashSaleRemainTime > 0){
            mTvPriceFree.setText(getString(R.string.txt_inapp_trial_descrition, SharedPreferenceHelper.getInstance(getApplicationContext()).getPriceByCurrency(Constants.PRICE_7_DAY_TRIAL_FLASH_SALE)));
            mTvPriceMonth.setText(SharedPreferenceHelper.getInstance(getApplicationContext()).getPriceByCurrency(Constants.PRICE_1_MONTH_FLASH_SALE));
            mTvPriceYear.setText(SharedPreferenceHelper.getInstance(getApplicationContext()).getPriceByCurrency(Constants.PRICE_1_YEAR_FLASH_SALE));
            mGroupFlashSale.setVisibility(View.VISIBLE);
            mGroupSubscription.setVisibility(View.GONE);
            mTvDayFlashSale.setText(Utilities.getDateFlashSale(this));
            setCountdownTimer(flashSaleRemainTime);
        } else {
            mTvPriceFree.setText(getString(R.string.txt_inapp_trial_descrition, SharedPreferenceHelper.getInstance(getApplicationContext()).getPriceByCurrency(Constants.PRICE_7_DAY_TRIAL)));
            mTvPriceMonth.setText(SharedPreferenceHelper.getInstance(getApplicationContext()).getPriceByCurrency(Constants.PRICE_1_MONTH));
            mTvPriceYear.setText(SharedPreferenceHelper.getInstance(getApplicationContext()).getPriceByCurrency(Constants.PRICE_1_YEAR));
            mGroupFlashSale.setVisibility(View.GONE);
            mGroupSubscription.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
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

                mTvDays.setText(days > 9 ? "" + days : "0" + days);
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
    protected void addListener() {
        hiddenNavRight();
        showNavLeft(R.drawable.ic_back, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mBtnFreeTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubscribeActivity.this, SubscriptionInAppActivity.class);
                intent.putExtra(Constants.IN_APP_TYPE, Constants.ENUM_FREE);
                startActivity(intent);

            }
        });
        mBtnMonth.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubscribeActivity.this, SubscriptionInAppActivity.class);
                intent.putExtra(Constants.IN_APP_TYPE, Constants.ENUM_MONTH);
                startActivity(intent);
            }
        });
        mBtnYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubscribeActivity.this, SubscriptionInAppActivity.class);
                intent.putExtra(Constants.IN_APP_TYPE, Constants.ENUM_YEAR);
                startActivity(intent);
            }
        });
        findViewById(R.id.btn_free_trial).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SubscribeActivity.this, SubscribeAboutActivity.class));
            }
        });
    }
}
