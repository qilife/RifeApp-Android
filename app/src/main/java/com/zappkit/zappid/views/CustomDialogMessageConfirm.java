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

public class CustomDialogMessageConfirm extends Dialog {
    private Context mContext;
    private Button mBtnCancel;
    private Button mBtnConfirm;
    private TextView mTvTitle, mTvContent, mTvVersion;
    private OnClickConfirmListener mOnClickConfirmListener;
    private OnClickCancelListener mOnClickCancelListener;
    private boolean mIsCanDismiss = true;


    public CustomDialogMessageConfirm(Context context) {
        super(context);
        mContext = context;
    }

    public void setCanDismiss(boolean isCanDismiss){
        mIsCanDismiss = isCanDismiss;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_message_confirm);
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

    public void setTextContent(String value) {
        mTvContent.setText(value);
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
        mTvContent = findViewById(R.id.tv_content);
        mTvVersion = findViewById(R.id.tv_version);

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickCancelListener != null) {
                    mOnClickCancelListener.onClick();
                }
                if(mIsCanDismiss) {
                    CustomDialogMessageConfirm.this.dismiss();
                }
            }
        });
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickConfirmListener != null) {
                    mOnClickConfirmListener.onClick("");
                }
                if(mIsCanDismiss) {
                    CustomDialogMessageConfirm.this.dismiss();
                }
            }
        });
    }

    public void setData(String title, String message, String version) {
        mTvTitle.setText(title);
        mTvContent.setText(message);
        mTvVersion.setText(version);
        mTvVersion.setVisibility(View.VISIBLE);
        mBtnCancel.setVisibility(View.GONE);
        mBtnConfirm.setText(mContext.getString(R.string.okay));
        findViewById(R.id.divider).setVisibility(View.GONE);
    }

    public interface OnClickConfirmListener {
        void onClick(String value);
    }

    public interface OnClickCancelListener {
        void onClick();
    }

}
