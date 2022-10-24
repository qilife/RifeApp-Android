package com.zappkit.zappid.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.api.models.GetFlashSaleOutput;
import com.zappkit.zappid.lemeor.tools.Constants;
import com.zappkit.zappid.lemeor.tools.QcAlarmManager;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;

import java.util.ArrayList;

public class AlarmReceiver extends BroadcastReceiver {
    public static int countNotification = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        int type = intent.getIntExtra(Constants.EXTRA_FLASH_SALE_TYPE, 0);

        String jsonFlashSale = SharedPreferenceHelper.getInstance(context).get(Constants.PREF_FLASH_SALE);
        GetFlashSaleOutput flashSale = new Gson().fromJson(jsonFlashSale, GetFlashSaleOutput.class);

        if (type == Constants.EXTRA_REMINDER_NOTIFICATION) {
            if (flashSale != null && flashSale.reminder != null) {
                int position = SharedPreferenceHelper.getInstance(context).getInt(Constants.PREF_REMINDER_NOTIFICATION_ITEM_POSITION);
                ArrayList<String> messages = flashSale.reminder.getMessages();
                String currentMessage = "";
                if (messages != null && messages.size() > 0) {
                    if (position < messages.size() - 1) {
                        position++;
                    } else {
                        position = 0;
                    }
                    currentMessage = messages.get(position);
                    SharedPreferenceHelper.getInstance(context).setInt(Constants.PREF_REMINDER_NOTIFICATION_ITEM_POSITION, position);
                }
                if (currentMessage != null && currentMessage.length() > 0) {
                    sendNotification(context, currentMessage, type);
                }
            }
            return;
        }

        if (flashSale != null && flashSale.flashSale != null) {
            if (type == Constants.EXTRA_FLASH_SALE_INIT) {
                SharedPreferenceHelper.getInstance(context).setInt(Constants.PREF_FLASH_SALE_COUNTERED, SharedPreferenceHelper.getInstance(context).getInt(Constants.PREF_FLASH_SALE_COUNTERED) + 1);

                if (SharedPreferenceHelper.getInstance(context).getInt(Constants.PREF_FLASH_SALE_COUNTERED) <= flashSale.flashSale.getProposalsCount()) {
                    Intent i = new Intent(Constants.ACTION_RECEIVE_FLASH_SALE_NOTIFICATION);
                    i.putExtra(Constants.EXTRA_FLASH_SALE_TYPE, type);
                    context.sendBroadcast(i);
                }
            } else {
                if (SharedPreferenceHelper.getInstance(context).getInt(Constants.PREF_FLASH_SALE_COUNTERED) == 0) {
                    SharedPreferenceHelper.getInstance(context).setInt(Constants.PREF_FLASH_SALE_COUNTERED, 1);
                }
                String message = "";
                switch (type) {
                    case Constants.EXTRA_FLASH_SALE_FIRST_NOTIFICATION:
                        if (flashSale.flashSale.getNtf() != null && flashSale.flashSale.getNtf().getFirst() != null) {
                            message = flashSale.flashSale.getNtf().getFirst().getMessage();
                        }
                        break;
                    case Constants.EXTRA_FLASH_SALE_SECOND_NOTIFICATION:
                        if (flashSale.flashSale.getNtf() != null && flashSale.flashSale.getNtf().getSecond() != null) {
                            message = flashSale.flashSale.getNtf().getSecond().getMessage();
                        }
                        break;
                    case Constants.EXTRA_FLASH_SALE_THIRD_NOTIFICATION:
                        if (flashSale.flashSale.getNtf() != null && flashSale.flashSale.getNtf().getThird() != null) {
                            message = flashSale.flashSale.getNtf().getThird().getMessage();
                        }
                        break;
                }

                if (message != null && message.length() > 0) {
                    sendNotification(context, message, type);
                }
            }
            if (SharedPreferenceHelper.getInstance(context).getInt(Constants.PREF_FLASH_SALE_COUNTERED) <= flashSale.flashSale.getProposalsCount()) {
                QcAlarmManager.createAlarms(context);
            } else {
                QcAlarmManager.clearAlarms(context);
            }
        } else {
            QcAlarmManager.clearAlarms(context);
        }
    }

    public void sendNotification(Context context, String message, int type) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        Intent intentMain = new Intent(context, INotificationBroadcast.class);
        intentMain.setAction(Long.toString(System.currentTimeMillis()));
        intentMain.putExtra(Constants.EXTRA_FLASH_SALE_TYPE, type);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intentMain, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (countNotification > 2000) {
            countNotification = 1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Qi Coil Channel ID";
            NotificationChannel channel = new NotificationChannel(channelId, "Qi Coil Channel", NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }
        mNotificationManager.notify(++countNotification, mBuilder.build());
    }
}