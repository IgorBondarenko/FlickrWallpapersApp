package com.beautiful_wallpapers_hd_qhd.core.receiver.updater;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.beautiful_wallpapers_hd_qhd.core.controller.SharedPreferencesController;
import com.beautiful_wallpapers_hd_qhd.core.service.UpdateWallpapersService;

/**
 * Created by Igor on 05.02.2015.
 */
public class UpdateWallpapersReceiver extends BroadcastReceiver {

    public static final int[] TIME_ARRAY = {1, 3, 7, 14, 28};

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, UpdateWallpapersService.class));
    }

    public void startUpdateWallpapersService(Context context){
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, UpdateWallpapersReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        int intervalIndex = new SharedPreferencesController(context).getInt(SharedPreferencesController.SP_WUS_INTERVAL, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), TIME_ARRAY[intervalIndex] * AlarmManager.INTERVAL_DAY, pi);
    }

    public void cancelUpdateWallpapersService(Context context){
        Intent intent = new Intent(context, UpdateWallpapersReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
