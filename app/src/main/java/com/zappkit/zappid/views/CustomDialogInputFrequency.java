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
import android.widget.EditText;

import com.zappkit.zappid.R;

public class CustomDialogInputFrequency extends Dialog {
    private Context mContext;
    private Button mBtnCancel;
    private Button mBtnConfirm;
    private EditText mEditInput;
    private IItemSelectedListener mIItemSelectedListener;


    public CustomDialogInputFrequency(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_input_frequency);
        setCancelable(false);
        Window window = this.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.flags &= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        this.getWindow().setAttributes(wlp);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        init();
    }

    public void setItemSelectedListener(IItemSelectedListener listener) {
        this.mIItemSelectedListener = listener;
    }

    public void setTextInput(int value) {
        mEditInput.setText(value + "");
    }

    public void init() {
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
        mEditInput = findViewById(R.id.edt_input);

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialogInputFrequency.this.dismiss();
            }
        });
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIItemSelectedListener != null) {
                    mIItemSelectedListener.onSelected(mEditInput.getText().toString().trim());
                }
                CustomDialogInputFrequency.this.dismiss();
            }
        });
    }

    public interface IItemSelectedListener {
        void onSelected(String value);
    }

}
