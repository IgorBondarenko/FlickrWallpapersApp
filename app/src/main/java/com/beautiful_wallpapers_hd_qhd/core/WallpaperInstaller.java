package com.beautiful_wallpapers_hd_qhd.core;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import com.edmodo.cropper.CropImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.IOException;

/**
 * Created by Igor on 03.02.2015.
 */
public class WallpaperInstaller {

    private ImageLoader imageLoader = ImageLoader.getInstance();
    private Context mContext;
    private boolean isCropped;

    private CropImageView cropImageView;

    public WallpaperInstaller(Context context, final CropImageView civ) {
        this.mContext = context;
        this.cropImageView = civ;
        this.isCropped = true;
    }

    public WallpaperInstaller(Context context) {
        this.mContext = context;
        this.isCropped = false;
    }

    public Thread setWallpaper(final String url){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap croppedBitmap;
                    if(isCropped){
                        int scale = 1;
                        RectF rect = cropImageView.getActualCropRect();
                        int cropX = (int) rect.left * scale;
                        int cropY = (int) rect.top * scale;
                        int cropW = (int) rect.width() * scale;
                        int cropH = (int) rect.height() * scale;
                        croppedBitmap = Bitmap.createBitmap(imageLoader.loadImageSync(url), cropX, cropY, cropW, cropH);
                    } else {
                        try{
                            croppedBitmap = Bitmap.createBitmap(imageLoader.loadImageSync(url));
                        } catch (NullPointerException e){
                            imageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
                            croppedBitmap = Bitmap.createBitmap(imageLoader.loadImageSync(url));
                        }
                    }
                    installWallpaper(croppedBitmap);

                } catch (IOException e) {
                    Log.d("WI", "setWallpaper:\n"+e.getMessage());
                }
            }
        });
    }

    public Thread setWallpapersFromService(final String url){
        return new Thread(() -> {
            if(!Thread.currentThread().isInterrupted()){
                imageLoader.loadImage(url, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        try {
                            installWallpaper(bitmap);
                        } catch (IOException e) {
                            Log.d("WI", "setWallpapersFromService:\n"+e.getMessage());
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });
            }
        });
    }

    private void installWallpaper(Bitmap image) throws IOException {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
        boolean isScrollable = image.getWidth() > image.getHeight();
        wallpaperManager.forgetLoadedWallpaper();
        wallpaperManager.clear();

        if(isScrollable){
            wallpaperManager.setWallpaperOffsetSteps(-1, -1);
            wallpaperManager.suggestDesiredDimensions(getWidth(image), getDisplay().getHeight());

            Log.d("WI", "image " + getWidth(image) + "x" + getDisplay().getHeight() + "\n" +
                    "display "+getDisplay().getWidth()+"x"+getDisplay().getHeight());

            wallpaperManager.setBitmap(Bitmap.createScaledBitmap(image, getWidth(image), getDisplay().getHeight(), false));
        } else{
            wallpaperManager.setWallpaperOffsetSteps(1, 1);
            wallpaperManager.suggestDesiredDimensions(getDisplay().getWidth(), getDisplay().getHeight());
            wallpaperManager.setBitmap(Bitmap.createScaledBitmap(image, getDisplay().getWidth(), getDisplay().getHeight(), false));
        }
    }

    private int getWidth(Bitmap bitmap){
        return (int)((float)getDisplay().getHeight()*(float)bitmap.getWidth()/(float)bitmap.getHeight());
    }

    private Display getDisplay(){
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay();
    }
}