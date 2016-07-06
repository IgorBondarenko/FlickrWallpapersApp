package com.beautiful_wallpapers_hd_qhd.core;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.beautiful_wallpapers_hd_qhd.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Igor on 20.02.2015.
 */
public class Device {

    private Context mContext;
    public static final String MAIN_DIRECTORY = "/Download/BeautifulWallpapersHD_QHD";

    public Device(Context c){
        this.mContext = c;
    }

    public boolean isTablet() {
        boolean xlarge = ((mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }

    public void downloadImage(Context context, String imageUrl, String title) {
        File direct = new File(Environment. getExternalStorageDirectory() + MAIN_DIRECTORY);

        if (!direct.exists()) {
            direct.mkdirs();
        }

        android.app.DownloadManager mgr = (android.app.DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(imageUrl);
        android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(downloadUri);

        Date date = new Date();
        SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        SimpleDateFormat format2 = new SimpleDateFormat("dd-MM-yyyy-hh-mm");

        try{
            request.setAllowedNetworkTypes(
                    android.app.DownloadManager.Request.NETWORK_WIFI
                            | android.app.DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle(format1.format(date) + " " + title)
                    .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(MAIN_DIRECTORY, format2.format(date) + "_" + title + ".jpg");

            mgr.enqueue(request);
            Toast.makeText(context, R.string.downloading_folder, Toast.LENGTH_LONG).show();
        } catch (IllegalStateException e){
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void checkAppsAndSave(String url) {
        if (isAppExist("cc.madkite.freedom")) {
            new Analytics(mContext).registerEvent("Preview", Analytics.FREEDOM, "detected", 0 );
            Intent intent = new Intent(mContext.getResources().getString(R.string.process_app_activity));
            mContext.startActivity(intent);
        } else {
            Intent saveIntent = new Intent(mContext.getResources().getString(R.string.save_activity));
            saveIntent.putExtra("url", url);
            mContext.startActivity(saveIntent);

            Activity activity = (Activity) mContext;
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        }
    }

    public void checkAppsAndBuy() {
        if (isAppExist("cc.madkite.freedom")) {
            new Analytics(mContext).registerEvent("Preview", Analytics.FREEDOM, "detected", 0);
            mContext.startActivity(new Intent(mContext.getResources().getString(R.string.process_app_activity)));
        } else {
            mContext.startActivity(new Intent(mContext.getResources().getString(R.string.choose_account_activity)));
            Activity activity = (Activity) mContext;
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        }
    }

    public List<String> getGmailAccounts() {
        Account[] accounts = AccountManager.get(mContext).getAccountsByType("com.google");
        List<String> gmailAccounts = new ArrayList<>();
        for (int i = 0; i < accounts.length; i++) {
            gmailAccounts.add(accounts[i].name);
        }
        return gmailAccounts;
    }

    public void checkOldApp(){
        if(isAppExist("photocollection.app")){
            mContext.startActivity(new Intent(mContext.getResources().getString(R.string.delete_old_version_activity)));
        }
    }

    private boolean isAppExist(String package_name){
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(package_name, 0);
            if(pi != null){
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
