package com.zappkit.zappid.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.zappkit.zappid.lemeor.MainMenuActivity;
import com.zappkit.zappid.lemeor.tools.Constants;

public class INotificationBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.hasExtra(Constants.EXTRA_FLASH_SALE_TYPE)) {
            int type = intent.getIntExtra(Constants.EXTRA_FLASH_SALE_TYPE, 0);
            if (type == 0){ return; }
            if (MainMenuActivity.ourMainRunning) {
                Intent i = new Intent(Constants.ACTION_RECEIVE_FLASH_SALE_NOTIFICATION);
                i.putExtra(Constants.EXTRA_FLASH_SALE_TYPE, type);
                context.sendBroadcast(i);
            } else {
                Intent ii = new Intent(context, MainMenuActivity.class);
                ii.putExtra(Constants.EXTRA_FLASH_SALE_TYPE, type);
                ii.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(ii);
            }
        }
    }
}
