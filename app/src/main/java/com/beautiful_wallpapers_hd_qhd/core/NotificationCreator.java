package com.beautiful_wallpapers_hd_qhd.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.activity.MainActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by Igor on 19.10.2015.
 */
public class NotificationCreator {

    private Context mContext;
    private Resources resources;
    private NotificationManager mNotificationManager;

    public NotificationCreator(Context context){
        this.mContext = context;
        this.resources = context.getResources();
        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void showLocalNotification(){
        Intent mainActivityIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(mContext, 0, mainActivityIntent, 0);

        NotificationCompat.Builder notification = notificationBuilder(resources.getString(R.string.notification_text), pi)
                .setTicker(resources.getString(R.string.notification_text));
        mNotificationManager.notify(0, notification.build());
    }

    private NotificationCompat.Builder notificationBuilder(String text, PendingIntent content){
        return new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_view_carousel_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher))
                .setContentIntent(content)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND);
    }
}
