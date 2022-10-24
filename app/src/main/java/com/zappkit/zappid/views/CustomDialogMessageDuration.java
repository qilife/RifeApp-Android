package com.zappkit.zappid.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.zappkit.zappid.R;

public class CustomDialogMessageDuration extends Dialog {
    private Context mContext;
    private Button mBtnCancel;
    private Button mBtnYes;
    private TextView mTvTitle, mTvContent;
    private OnClickYesListener mOnClickYesListener;
    private OnClickCancelListener mOnClickCancelListener;

    public CustomDialogMessageDuration(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_message_duration);
        setCancelable(false);
        Window window = this.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(wlp);
        this.getWindow().setAttributes(wlp);
        init();
    }

    public void setmBtnCancel(Button mBtnCancel) {
        this.mBtnCancel = mBtnCancel;
    }

    public void setmBtnYes(Button mBtnYes) {
        this.mBtnYes = mBtnYes;
    }

    public void setmTvTitle(TextView mTvTitle) {
        this.mTvTitle = mTvTitle;
    }

    public void setmTvContent(TextView mTvContent) {
        this.mTvContent = mTvContent;
    }

    public void setmOnClickYesListener(OnClickYesListener mOnClickYesListener) {
        this.mOnClickYesListener = mOnClickYesListener;
    }

    public void setmOnClickCancelListener(OnClickCancelListener mOnClickCancelListener) {
        this.mOnClickCancelListener = mOnClickCancelListener;
    }

    private void init() {
        mBtnCancel = findViewById(R.id.btn_cancel);
        mBtnYes = findViewById(R.id.btn_yes);
        mTvTitle = findViewById(R.id.tv_title);
        mTvContent = findViewById(R.id.tv_content);

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialogMessageDuration.this.dismiss();
            }
        });
        mBtnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickYesListener != null) {
                    mOnClickYesListener.onClick("");
                }
                CustomDialogMessageDuration.this.dismiss();
            }
        });
    }

    public void setData(String title, String message) {
        mTvTitle.setText(title);
        mTvContent.setText(message);
        mBtnCancel.setVisibility(View.GONE);
        mBtnYes.setText(mContext.getString(R.string.okay));
        findViewById(R.id.divider).setVisibility(View.GONE);
    }

    public interface OnClickYesListener {
        void onClick(String value);
    }

    public interface OnClickCancelListener {
        void onClick();
    }
}
