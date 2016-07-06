package com.beautiful_wallpapers_hd_qhd.core.receiver.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Igor on 17.10.2014.
 */
public class NotificationOnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new NotificationReceiver().start(context);
    }

}
