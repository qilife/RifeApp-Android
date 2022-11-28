package com.zappkit.zappid.lemeor.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.google.gson.Gson;
import com.zappkit.zappid.lemeor.api.models.GetFlashSaleOutput;
import com.zappkit.zappid.services.AlarmReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class QcAlarmManager {
    public static int countAlarm = 0;

    public static void createAlarms (Context context) {
        if (SharedPreferenceHelper.getInstance(context).getBool(Constants.KEY_PURCHASED)) {
            clearAlarms(context);
            return;
        }
        String jsonFlashSale = SharedPreferenceHelper.getInstance(context).get(Constants.PREF_FLASH_SALE);
        GetFlashSaleOutput flashsale = new Gson().fromJson(jsonFlashSale, GetFlashSaleOutput.class);
        clearAlarms(context);
        if (flashsale.flashSale != null) {
            if (flashsale.flashSale.isEnable()) {

                float initDelay = flashsale.flashSale.getInitDelay();
                float interval = flashsale.flashSale.getInterval();

                Calendar currentCal = Calendar.getInstance();
                long fistIntallerAppTime = SharedPreferenceHelper.getInstance(context).getLong(Constants.EXTRA_FIRST_INSTALLER_APP_TIME);
                long initFSTime = fistIntallerAppTime + (long)(initDelay * 60 * 60 * 1000);
                Calendar calInitFSTime = Calendar.getInstance();
                calInitFSTime.setTimeInMillis(initFSTime);
                createNewAlarms(context, currentCal, calInitFSTime, interval, Constants.EXTRA_FLASH_SALE_INIT);

                if (flashsale.flashSale.getNtf() != null) {
                    if (flashsale.flashSale.getNtf().getFirst() != null) {
                        long firstFlashSale = initFSTime + (long)(flashsale.flashSale.getNtf().getFirst().getDelay() * 60 * 60 * 1000);
                        Calendar calFirstFS = Calendar.getInstance();
                        calFirstFS.setTimeInMillis(firstFlashSale);
                        createNewAlarms(context, currentCal, calFirstFS, interval, Constants.EXTRA_FLASH_SALE_FIRST_NOTIFICATION);
                    }

                    if (flashsale.flashSale.getNtf().getSecond() != null) {
                        long secondFlashSale = initFSTime + (long)(flashsale.flashSale.getNtf().getSecond().getDelay() * 60 * 60 * 1000);
                        Calendar calSecondFS = Calendar.getInstance();
                        calSecondFS.setTimeInMillis(secondFlashSale);
                        createNewAlarms(context, currentCal, calSecondFS, interval, Constants.EXTRA_FLASH_SALE_SECOND_NOTIFICATION);
                    }

                    if (flashsale.flashSale.getNtf().getThird() != null) {
                        long thirdFlashSale = initFSTime + (long)(flashsale.flashSale.getNtf().getThird().getDelay() * 60 * 60 * 1000);
                        Calendar calThirdFS = Calendar.getInstance();
                        calThirdFS.setTimeInMillis(thirdFlashSale);
                        createNewAlarms(context, currentCal, calThirdFS, interval, Constants.EXTRA_FLASH_SALE_THIRD_NOTIFICATION);
                    }
                }
            } else {
                QcAlarmManager.clearAlarms(context);
            }
        } else {
            QcAlarmManager.clearAlarms(context);
        }
    }

    public static void createNewAlarms(Context context, Calendar currentCal, Calendar alarmCal, Float interval, int flashSaleType) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        while (alarmCal.before(currentCal)) {
            alarmCal.add(Calendar.SECOND, (int)(interval * 24 * 60 * 60));
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.EXTRA_FLASH_SALE_TYPE, flashSaleType);
        if (countAlarm > 20) {
            countAlarm = 0;
        }
        countAlarm++;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, countAlarm, intent, getPendingIntentFlags());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCal.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmCal.getTimeInMillis(), (long)(interval * 24 * 60 * 60 * 1000), pendingIntent);
        }
    }

    public static int getPendingIntentFlags() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;
    }

    public static void clearAlarms(Context context) {
        for (int i = 0; i <= 21; i++) {
            Intent intent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, i, intent, getPendingIntentFlags());
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }
    }

    public static void createReminderAlarm(Context context) {
        //Remove
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.EXTRA_FLASH_SALE_TYPE, Constants.EXTRA_REMINDER_NOTIFICATION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Constants.REMINDER_NOTIFICATION_ID, intent, getPendingIntentFlags());
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        //Re-create reminder
        String jsonFlashSale = SharedPreferenceHelper.getInstance(context).get(Constants.PREF_FLASH_SALE);
        if (jsonFlashSale != null) {
            GetFlashSaleOutput flashsale = new Gson().fromJson(jsonFlashSale, GetFlashSaleOutput.class);
            if (flashsale != null && flashsale.reminder != null && flashsale.reminder.getMessages() != null && flashsale.reminder.getMessages().size() > 0) {
                SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
                Date date;
                try {
                    date = hourFormat.parse(flashsale.reminder.getLaunchTime());
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, date.getHours());
                    calendar.set(Calendar.MINUTE, date.getMinutes());
                    calendar.set(Calendar.SECOND, 0);
                    Calendar currentCalender = Calendar.getInstance();
                    if (calendar.getTimeInMillis() < currentCalender.getTimeInMillis()) {
                        calendar.add(Calendar.DATE, 1);
                        calendar.set(Calendar.HOUR_OF_DAY, date.getHours());
                        calendar.set(Calendar.MINUTE, date.getMinutes());
                        calendar.set(Calendar.SECOND, 0);
                    }

                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), (long)(flashsale.reminder.getInterval() * 60 * 60 * 1000), pendingIntent);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}