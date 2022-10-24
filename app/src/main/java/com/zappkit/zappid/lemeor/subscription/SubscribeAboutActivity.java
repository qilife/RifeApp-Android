package com.zappkit.zappid.lemeor.subscription;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.base.BaseActivity;
import com.zappkit.zappid.lemeor.tools.Utilities;
import com.zappkit.zappid.lemeor.tools.Constants;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;

public class SubscribeAboutActivity extends BaseActivity {
    private TextView mTvPriceFree;
    private TextView mTvPriceMonth;
    private TextView mTvPriceYear;

    @Override
    protected int initLayout() {
        return R.layout.activity_subscribe_about;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initComponents() {
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(R.string.txt_subcrise);
        TextView tvLinkPolicy = findViewById(R.id.tv_link_policy);
        TextView tvLinkTerm = findViewById(R.id.tv_link_term);
        tvLinkPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable sp = (Spannable) tvLinkPolicy.getText();
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

        tvLinkTerm.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable sp1 = (Spannable) tvLinkTerm.getText();
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
        mTvPriceFree = findViewById(R.id.tv_price_7_day);
        mTvPriceMonth = findViewById(R.id.tv_price_month);
        mTvPriceYear = findViewById(R.id.tv_price_year);

        loadPrices();
    }

    public void loadPrices(){
        long flashSaleRemainTime = Utilities.getFlaseSaleRemainTime(this);
        if (flashSaleRemainTime > 0) {
            setCountdownTimer(flashSaleRemainTime);
        }
        mTvPriceFree.setText("• 7 day free trial, then " + SharedPreferenceHelper.getInstance(getApplicationContext()).getPriceByCurrency(flashSaleRemainTime > 0 ? Constants.PRICE_7_DAY_TRIAL_FLASH_SALE : Constants.PRICE_7_DAY_TRIAL) + " per month");
        mTvPriceMonth.setText("• 1 Month subscription - " + SharedPreferenceHelper.getInstance(getApplicationContext()).getPriceByCurrency(flashSaleRemainTime > 0 ? Constants.PRICE_1_MONTH_FLASH_SALE : Constants.PRICE_1_MONTH) + " per month");
        mTvPriceYear.setText("• Annual subscription - " + SharedPreferenceHelper.getInstance(getApplicationContext()).getPriceByCurrency(flashSaleRemainTime > 0 ? Constants.PRICE_1_YEAR_FLASH_SALE : Constants.PRICE_1_YEAR) + " per year");
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
    }

    private CountDownTimer mCountDownTimer;

    public void setCountdownTimer(long totalTime) {
        mCountDownTimer = new CountDownTimer(totalTime, 1000) {
            @Override
            public void onTick(long l) {

            }
            @Override

            public void onFinish() {
                loadPrices();
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
