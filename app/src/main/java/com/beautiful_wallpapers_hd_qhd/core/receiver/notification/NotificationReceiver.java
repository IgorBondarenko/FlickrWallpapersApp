package com.beautiful_wallpapers_hd_qhd.core.receiver.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.beautiful_wallpapers_hd_qhd.core.NotificationCreator;

/**
 * Created by Igor on 17.10.2014.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new NotificationCreator(context).showLocalNotification();
    }

    public void start(Context context){
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        long interval = AlarmManager.INTERVAL_DAY * 7;
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pi);
    }
}
