package com.zappkit.zappid.lemeor.subscription;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.base.BaseActivity;
import com.zappkit.zappid.lemeor.tools.Constants;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import com.zappkit.zappid.lemeor.tools.Utilities;

public class SubscriptionInAppActivity extends BaseActivity {

    @Override
    protected int initLayout() {
        return R.layout.activity_subscribe_in_app;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initComponents() {
        TextView mTvTitle = findViewById(R.id.tv_title);
        mTvTitle.setText(R.string.txt_subcrise);
        TextView mTvLinkPolicy = findViewById(R.id.tv_link_policy);
        TextView mTvLinkTerm = findViewById(R.id.tv_link_term);
        mTvLinkPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable sp = (Spannable) mTvLinkPolicy.getText();
        ClickableSpan click = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String url = "http://www.tattoobookapp.com/quantumwavebiotechnology/privacy";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        };
        sp.setSpan(click, 17, sp.length(), 0);

        mTvLinkTerm.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable sp1 = (Spannable) mTvLinkTerm.getText();
        ClickableSpan click1 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String url = "http://www.tattoobookapp.com/quantumwavebiotechnology/terms";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        };
        sp1.setSpan(click1, 15, sp1.length(), 0);

        loadInfor();
    }

    @SuppressLint("SetTextI18n")
    public void loadInfor(){
        Button mBtnFree = findViewById(R.id.btn_sub_free);
        TextView mTvLength = findViewById(R.id.tv_length);
        TextView mTvPrice = findViewById(R.id.tv_price);

        long flashSaleRemainTime = Utilities.getFlaseSaleRemainTime(this);
        if (flashSaleRemainTime > 0) {
            setCountdownTimer(flashSaleRemainTime);
        }
        String inAppType = getIntent().getStringExtra(Constants.IN_APP_TYPE);
        if (inAppType != null && inAppType.equalsIgnoreCase(Constants.ENUM_FREE)) {
            mBtnFree.setText(R.string.tv_start_free_trial);
            mBtnFree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long flashSaleRemainTime = Utilities.getFlaseSaleRemainTime(SubscriptionInAppActivity.this);
                    if(flashSaleRemainTime > 0){
                        onPurchaseProduct(Constants.SKU_RIFE_FREE_FLASH_SALE);
                    } else {
                        onPurchaseProduct(Constants.SKU_RIFE_FREE);
                    }
                }
            });
            mTvLength.setText(R.string.tv_lenght_free);
            mTvPrice.setText("• Free trial - 7 days free, then " +
                    SharedPreferenceHelper.getInstance(getApplicationContext()).getPriceByCurrency(flashSaleRemainTime > 0 ? Constants.PRICE_7_DAY_TRIAL_FLASH_SALE : Constants.PRICE_7_DAY_TRIAL) +
                    " per month. The price corresponds to the same price segment, which are set in the Android Google Play for other currencies");
        } else if (inAppType != null && inAppType.equalsIgnoreCase(Constants.ENUM_MONTH)) {
            mBtnFree.setText(R.string.tv_start_monthly_subscription);
            mBtnFree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long flashSaleRemainTime = Utilities.getFlaseSaleRemainTime(SubscriptionInAppActivity.this);
                    if(flashSaleRemainTime > 0){
                        onPurchaseProduct(Constants.SKU_RIFE_MONTHLY_FLASH_SALE);
                    } else {
                        onPurchaseProduct(Constants.SKU_RIFE_MONTHLY);
                    }
                }
            });
            mTvLength.setText(R.string.tv_length_month);
            mTvPrice.setText("• " + SharedPreferenceHelper.getInstance(getApplicationContext()).getPriceByCurrency(flashSaleRemainTime > 0 ? Constants.PRICE_1_MONTH_FLASH_SALE : Constants.PRICE_1_MONTH)
                    + " per month. The price corresponds to the same price segment, which are set in the Android Google Play for other currencies");
        } else if (inAppType != null && inAppType.equalsIgnoreCase(Constants.ENUM_YEAR)) {
            mBtnFree.setText(R.string.tv_start_yearly_subscription);
            mBtnFree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long flashSaleRemainTime = Utilities.getFlaseSaleRemainTime(SubscriptionInAppActivity.this);
                    if(flashSaleRemainTime > 0){
                        onPurchaseProduct(Constants.SKU_RIFE_YEARLY_FLASH_SALE);
                    } else {
                        onPurchaseProduct(Constants.SKU_RIFE_YEARLY);
                    }
                }
            });
            mTvLength.setText(R.string.tv_length_year);
            mTvPrice.setText("• " + SharedPreferenceHelper.getInstance(getApplicationContext()).getPriceByCurrency(flashSaleRemainTime > 0 ? Constants.PRICE_1_YEAR_FLASH_SALE : Constants.PRICE_1_YEAR)
                    + " per year. The price corresponds to the same price segment, which are set in the Android Google Play for other currencies");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void addListener() {
        hiddenNavRight();
        showNavLeft(R.drawable.ic_back, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private CountDownTimer mCountDownTimer;

    public void setCountdownTimer(long totalTime) {
        mCountDownTimer = new CountDownTimer(totalTime, 1000) {
            @Override
            public void onTick(long l) {

            }
            @Override
            public void onFinish() {
                loadInfor();
            }
        };
        mCountDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }
}
