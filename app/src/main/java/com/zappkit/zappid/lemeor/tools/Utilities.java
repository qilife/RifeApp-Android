package com.zappkit.zappid.lemeor.tools;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.PlayListListModel;
import com.zappkit.zappid.lemeor.models.SequenceListModel;
import com.zappkit.zappid.lemeor.api.models.GetFlashSaleOutput;
import com.zappkit.zappid.lemeor.main_menu.player.FrequencyUIActivity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

public class Utilities {

    public static void addProgramToPlayer(Context context, String pos, int seqDbId) {
        DbHelper database = new DbHelper(context);
        SharedPreferences settings = SharedPreferenceHelper.getSharedPreferences(context);
        String currentLang = settings.getString("language", "en");
        String locale = "";
        if (!currentLang.equals("en")) { locale = "_" + currentLang; }

        stopPlayer(context, database, settings);

        int seqId = Integer.parseInt(pos);
        Cursor values = database.getSequence(Integer.toString(seqId), seqDbId);
        values.moveToFirst();

        String seqName = values.getString(values.getColumnIndex("name" + locale));
        String[] freq_id = SequenceFrequencies(values.getString(values.getColumnIndex("list")));

        for (String i : freq_id) {
            String[] ids = i.split(",");
            if (ids.length >= 2) {
                database.addToPlaylist(ids[1], Integer.parseInt(ids[0]), seqName, seqId, seqDbId);
            }
        }

        database.ChangeFirstItemState("pause", Math.ceil(settings.getInt("FreqDuration", 180)));

        SharedPreferenceHelper.getSharedPreferences(context).edit().putBoolean("isPlaying", false).apply();
        Intent intent = new Intent(context, FrequencyUIActivity.class);
        intent.putExtra("stop", true);
        context.startActivity(intent);
    }

    public static void addFrequencyToPlayer(Context context, String id, int seqDbId) {
        DbHelper database = new DbHelper(context);
        SharedPreferences settings = SharedPreferenceHelper.getSharedPreferences(context);

        stopPlayer(context, database, settings);

        database.addToPlaylist(id, seqDbId, "", -1, 1);
        database.ChangeFirstItemState("pause", Math.ceil(settings.getInt("FreqDuration", 180)));
        SharedPreferenceHelper.getSharedPreferences(context).edit().putBoolean("isPlaying", false).apply();
        Intent intent = new Intent(context, FrequencyUIActivity.class);
        intent.putExtra("stop", true);
        context.startActivity(intent);
    }




    public static void createKeyHash(Context context) {
        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException ignored) { }
        catch (NoSuchAlgorithmException ignored) { }
    }

    public static long getFlaseSaleRemainTime(Context context) {
        long flashSaleRemainTime = 0L;
        String jsonFlashSale = SharedPreferenceHelper.getInstance(context).get(Constants.PREF_FLASH_SALE);
        if (jsonFlashSale != null && jsonFlashSale.length() > 0) {
            GetFlashSaleOutput flashsale = new Gson().fromJson(jsonFlashSale, GetFlashSaleOutput.class);
            if (flashsale.flashSale != null) {
                if (flashsale.flashSale.isEnable()) {
                    float initDelay = flashsale.flashSale.getInitDelay();
                    float duration = flashsale.flashSale.getDuration();
                    float interval = flashsale.flashSale.getInterval();

                    Calendar currentCal = Calendar.getInstance();

                    long fistIntallerAppTime = SharedPreferenceHelper.getInstance(context).getLong(Constants.EXTRA_FIRST_INSTALLER_APP_TIME);
                    long initFSTime = fistIntallerAppTime + (long) (initDelay * 60 * 60 * 1000);

                    if (currentCal.getTimeInMillis() - fistIntallerAppTime >= (long) (initDelay * 60 * 60 * 1000)) {
                        Calendar calInitFSTime = Calendar.getInstance();
                        calInitFSTime.setTimeInMillis(initFSTime);

                        int count = 0;
                        while (calInitFSTime.before(currentCal)) {
                            count++;
                            calInitFSTime.add(Calendar.SECOND, (int) (interval * 24 * 60 * 60));
                        }
                        if (count > 0) {
                            calInitFSTime.add(Calendar.SECOND, -1 * (int) (interval * 24 * 60 * 60));
                        }
                        if (SharedPreferenceHelper.getInstance(context).getInt(Constants.PREF_FLASH_SALE_COUNTERED) <= flashsale.flashSale.getProposalsCount()) {
                            flashSaleRemainTime = calInitFSTime.getTimeInMillis() + (long) (duration * 60 * 60 * 1000) - Calendar.getInstance().getTimeInMillis();
                        }
                    }
                }
            }
        }
        return flashSaleRemainTime;
    }

    public static String getDateFlashSale(Context context) {
        String timeString = "April 4th";
        SimpleDateFormat formatMonth = new SimpleDateFormat("MMMM");
        SimpleDateFormat formatDate = new SimpleDateFormat("d");
        try {
            timeString = formatMonth.format(Calendar.getInstance().getTime()) + " " + getDays(Integer.valueOf(formatDate.format(Calendar.getInstance().getTime())));
            String jsonFlashSale = SharedPreferenceHelper.getInstance(context).get(Constants.PREF_FLASH_SALE);
            if (jsonFlashSale != null && jsonFlashSale.length() > 0) {
                GetFlashSaleOutput flashsale = new Gson().fromJson(jsonFlashSale, GetFlashSaleOutput.class);
                if (flashsale.flashSale != null) {
                    if (flashsale.flashSale.isEnable()) {
                        float initDelay = flashsale.flashSale.getInitDelay();
                        float duration = flashsale.flashSale.getDuration();
                        float interval = flashsale.flashSale.getInterval();

                        Calendar currentCal = Calendar.getInstance();

                        long fistIntallerAppTime = SharedPreferenceHelper.getInstance(context).getLong(Constants.EXTRA_FIRST_INSTALLER_APP_TIME);
                        long initFSTime = fistIntallerAppTime + (long) (initDelay * 60 * 60 * 1000);

                        if (currentCal.getTimeInMillis() - fistIntallerAppTime >= (long) (initDelay * 60 * 60 * 1000)) {
                            Calendar calInitFSTime = Calendar.getInstance();
                            calInitFSTime.setTimeInMillis(initFSTime);

                            int count = 0;
                            while (calInitFSTime.before(currentCal)) {
                                count++;
                                calInitFSTime.add(Calendar.SECOND, (int) (interval * 24 * 60 * 60));
                            }
                            if (count > 0) {
                                calInitFSTime.add(Calendar.SECOND, -1 * (int) (interval * 24 * 60 * 60));
                            }
                            long flashSaleTime = calInitFSTime.getTimeInMillis() + (long) (duration * 60 * 60 * 1000);
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(flashSaleTime);
                            timeString = formatMonth.format(cal.getTime()) + " " + getDays(Integer.valueOf(formatDate.format(cal.getTime())));

                        }
                    }
                }
            }
        } catch (NumberFormatException e) {

        } catch (Exception ex) {

        }
        return timeString;
    }

    public static String getDays(int day) {
        String dayString;
        switch (day) {
            case 1:
            case 21:
            case 31:
                dayString = day + "st";
                break;
            case 2:
            case 22:
                dayString = day + "nd";
                break;
            case 3:
            case 23:
                dayString = day + "rd";
                break;
            default:
                dayString = day + "th";
                break;
        }
        return dayString;
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory() && fileOrDirectory.listFiles() != null)
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    public static void addPlaylistToPlayer(Context context, PlayListListModel playListListModel) {
        DbHelper database;
        SharedPreferences settings;
        String currentlang;
        String locale;
        database = new DbHelper(context);
        settings = SharedPreferenceHelper.getSharedPreferences(context);
        currentlang = settings.getString("language", "en");
        locale = "";
        if (!currentlang.equals("en")) {
            locale = "_" + currentlang;
        }

        stopPlayer(context, database, settings);


        String[] sequences = playListListModel.getList().split("-");
        ArrayList<SequenceListModel> sequencesList = new ArrayList<>();
        for (String seq : sequences) {
            SequenceListModel tempSeqModel = new SequenceListModel();
            String[] tempIds = seq.split(",");
            Cursor tempSeqCursor = database.getSequence(tempIds[1], Integer.parseInt(tempIds[0]));
            tempSeqCursor.moveToFirst();
            tempSeqModel.setDbId(Integer.parseInt(tempIds[0]));
            tempSeqModel.setId(Integer.parseInt(tempIds[1]));
            tempSeqModel.setSequenceTitle(tempSeqCursor.getString(tempSeqCursor.getColumnIndex("name" + locale)));
            tempSeqModel.setNotes("");
            sequencesList.add(tempSeqModel);
            tempSeqCursor.close();
        }
        if (sequencesList.size() > 0) {
            Iterator it = sequencesList.iterator();
            while (it.hasNext()) {
                SequenceListModel playModel = (SequenceListModel) it.next();
                Cursor tempCursor = database.getSequence(playModel.getIdString(), playModel.getDatabaseId());
                tempCursor.moveToFirst();
                for (String id : tempCursor.getString(tempCursor.getColumnIndex("list")).split("-")) {
                    String[] tempIds = id.split(",");
                    if (tempIds.length > 1) {
                        database.addToPlaylist(tempIds[1], Integer.parseInt(tempIds[0]), playModel.getSequenceTitle(), playModel.getId(), playModel.getDatabaseId());
                    }
                }
            }
        }


        database.ChangeFirstItemState("pause", Math.ceil((double) settings.getInt("FreqDuration", 180)));

        SharedPreferenceHelper.getSharedPreferences(context).edit().putBoolean("isPlaying", false).commit();
        Intent intent = new Intent(context, FrequencyUIActivity.class);
        intent.putExtra("stop", true);
        context.startActivity(intent);
    }



    public static void stopPlayer(Context context, DbHelper database, SharedPreferences settings) {
        Cursor PlaylistItems = database.getPlayListItemsCursor();
        if (PlaylistItems.moveToFirst() && PlaylistItems.getString(PlaylistItems.getColumnIndex("state")).equals("play")) {
            Intent intent = new Intent("PauseFreq");
            intent.putExtra("stop", true);
            context.sendBroadcast(intent);
        }
        PlaylistItems.close();

        Intent intent = new Intent("PauseFreq");
        intent.putExtra("stop", true);
        context.sendBroadcast(intent);

        database.empty_playlist();
        if (settings.getBoolean("notifications", true)) {
            ((NotificationManager) context.getSystemService(FrequencyUIActivity.NOTIFICATION_SERVICE)).cancelAll();
        }
    }

    public static void stopPlayerInSetting(Context context) {
        DbHelper database = new DbHelper(context);
        SharedPreferences settings = SharedPreferenceHelper.getSharedPreferences(context);
        database.ResetAllItemState();

        Intent intent = new Intent("PauseFreq");
        intent.putExtra("stop", true);
        context.sendBroadcast(intent);

        if (settings.getBoolean("notifications", true)) {
            ((NotificationManager) context.getSystemService(FrequencyUIActivity.NOTIFICATION_SERVICE)).cancelAll();
        }
        SharedPreferenceHelper.getSharedPreferences(context).edit().putBoolean("isPlaying", false).commit();
        database.ChangeFirstItemState("pause", Math.ceil((double) settings.getInt("FreqDuration", 180)));
    }

    public static String[] SequenceFrequencies(String unaltered) {
        return unaltered.split("-");
    }

    public static String getParamsRequest(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        if (params.size() > 0) {
            for (String key : params.keySet()) {
                if (builder.length() > 0) {
                    builder.append("&");
                }
                builder.append(key).append("=");
                try {
                    builder.append(URLEncoder.encode(params.get(key), Constants.CHARSET));
                } catch (UnsupportedEncodingException e) {
                    builder.append(params.get(key));
                }
            }
        }
        return builder.toString();
    }

    public static boolean deleteFileInDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteFileInDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String timeCalc(int sec) {
        int m = 0;
        int h = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while (sec >= 60) {
            m++;
            sec -= 60;
        }
        int s = sec;
        if (m >= 60) {
            while (m >= 60) {
                h++;
                m -= 60;
            }
            stringBuilder.append(Integer.toString(h) + ":");
            if (m == 0) {
                stringBuilder.append(Integer.toString(m) + ":");
            }
        }
        if (m != 0) {
            stringBuilder.append(Integer.toString(m) + ":");
        } else {
            stringBuilder.append("0:");
        }
        stringBuilder.append((s < 10 ? "0" : "") + Integer.toString(s) + "");
        return stringBuilder.toString();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
