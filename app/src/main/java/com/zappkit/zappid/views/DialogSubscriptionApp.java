package com.zappkit.zappid.views;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.subscription.SubscribeActivity;
import com.zappkit.zappid.lemeor.tools.Constants;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;

public class DialogSubscriptionApp extends Dialog {
    private Context mContext;
    private Button mBtnFree;
    private Button mBtnLater;

    private BroadcastReceiver onControllDialogSubscription = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SharedPreferenceHelper.getInstance(mContext).getBool(Constants.KEY_PURCHASED)){
                dismiss();
            }
        }
    };


    public DialogSubscriptionApp(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_subsription);
        setCancelable(false);
        Window window = this.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.flags &= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(wlp);
        this.getWindow().setAttributes(wlp);
        mContext.registerReceiver(onControllDialogSubscription, new IntentFilter(Constants.BROADCAST_ACTION_PURCHASED));
        init();
    }

    public void init() {
        mBtnFree = findViewById(R.id.btn_free);
        mBtnLater = findViewById(R.id.btn_later);

        mBtnFree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SubscribeActivity.class);
                intent.putExtra(Constants.ENUM_FREE, true);
                mContext.startActivity(intent);
                dismiss();
            }
        });
        mBtnLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    @Override
    public void dismiss() {
        try {
            mContext.unregisterReceiver(onControllDialogSubscription);
        }catch (Exception e){
            Log.e("Error","Error" + e.toString());
            e.printStackTrace();
        }
        super.dismiss();
    }
}
