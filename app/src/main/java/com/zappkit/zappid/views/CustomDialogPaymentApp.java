package com.zappkit.zappid.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.tools.Constants;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import com.zappkit.zappid.lemeor.tools.Utilities;

import java.util.Calendar;

public class CustomDialogPaymentApp extends Dialog {
    private Context mContext;
    private Button mBtnCancel;
    private Button mBtnConfirm;
    private TextView mTvTitle;
    private OnClickConfirmListener mOnClickConfirmListener;
    private OnClickCancelListener mOnClickCancelListener;
    private boolean mIsCanDismiss = true;


    public CustomDialogPaymentApp(Context context) {
        super(context);
        mContext = context;
    }

    public void setCanDismiss(boolean isCanDismiss) {
        mIsCanDismiss = isCanDismiss;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_payment_app);
        setCancelable(false);
        Window window = this.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.flags &= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(wlp);
        this.getWindow().setAttributes(wlp);
        init();
    }

    public void setOnClickConfirmListener(OnClickConfirmListener listener) {
        this.mOnClickConfirmListener = listener;
    }

    public void setOnClickCancelListener(OnClickCancelListener listener) {
        this.mOnClickCancelListener = listener;
    }

    public void setTextTitle(String value) {
        mTvTitle.setText(value);
    }


    public void setTextButtonLeft(String value) {
        mBtnCancel.setText(value);
    }

    public void setTextButtonRight(String value) {
        mBtnConfirm.setText(value);
    }

    public void init() {
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
        mTvTitle = findViewById(R.id.tv_title);

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickCancelListener != null) {
                    mOnClickCancelListener.onClick();
                }
                if (mIsCanDismiss) {
                    CustomDialogPaymentApp.this.dismiss();
                }
            }
        });
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickConfirmListener != null) {
                    mOnClickConfirmListener.onClick("");
                }
                if (mIsCanDismiss) {
                    CustomDialogPaymentApp.this.dismiss();
                }
            }
        });
    }

    private CountDownTimer mCountDownTimer;

    public void setCountdownTimer(){
        long limitTime = Calendar.getInstance().getTimeInMillis() - SharedPreferenceHelper.getInstance(mContext).getLong(Constants.PRE_LIMIT_TIME_PLAYER);
        if (limitTime < Constants.ONE_HOUR_TIME) {
            mCountDownTimer = new CountDownTimer(Constants.ONE_HOUR_TIME - limitTime, 1000) {
                @Override
                public void onTick(long l) {
                    int second = (int) (l/1000);
                    mBtnConfirm.setText("Waiting " + Utilities.timeCalc(second));
                }

                @Override
                public void onFinish() {
                    dismiss();
                }
            };
            mCountDownTimer.start();
        }
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }
        }catch (IllegalArgumentException ex){

        }
    }

    public interface OnClickConfirmListener {
        void onClick(String value);
    }

    public interface OnClickCancelListener {
        void onClick();
    }

}
