package com.beautiful_wallpapers_hd_qhd.core.entity;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.beautiful_wallpapers_hd_qhd.R;

/**
 * Created by Igor on 19.04.2016.
 */
public class FlickrImageEXIF implements Parcelable {

    private String camera;
    private String resolution;
    private String aperture;
    private String focalLength;
    private String iso;
    private String exposureTime;
    private String whiteBalance;
    private String meteringMode;
    private String exposureMode;
    private boolean isLocked = false;

    public FlickrImageEXIF(){

    }

    protected FlickrImageEXIF(Parcel in) {
        camera = in.readString();
        resolution = in.readString();
        aperture = in.readString();
        focalLength = in.readString();
        iso = in.readString();
        exposureTime = in.readString();
        whiteBalance = in.readString();
        meteringMode = in.readString();
        exposureMode = in.readString();
    }

    public static final Creator<FlickrImageEXIF> CREATOR = new Creator<FlickrImageEXIF>() {
        @Override
        public FlickrImageEXIF createFromParcel(Parcel in) {
            return new FlickrImageEXIF(in);
        }

        @Override
        public FlickrImageEXIF[] newArray(int size) {
            return new FlickrImageEXIF[size];
        }
    };

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getAperture() {
        return aperture;
    }

    public void setAperture(String aperture) {
        this.aperture = aperture;
    }

    public String getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(String focalLength) {
        this.focalLength = focalLength;
    }

    public String getISO() {
        return iso;
    }

    public void setISO(String iso) {
        this.iso = iso;
    }

    public String getExposureTime() {
        return exposureTime;
    }

    public void setExposureTime(String exposureTime) {
        this.exposureTime = exposureTime;
    }

    public String getWhiteBalance() {
        return whiteBalance;
    }

    public void setWhiteBalance(String whiteBalance) {
        this.whiteBalance = whiteBalance;
    }

    public String getMeteringMode() {
        return meteringMode;
    }

    public void setMeteringMode(String meteringMode) {
        this.meteringMode = meteringMode;
    }

    public String getExposureMode() {
        return exposureMode;
    }

    public void setExposureMode(String exposureMode) {
        this.exposureMode = exposureMode;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(camera);
        dest.writeString(resolution);
        dest.writeString(aperture);
        dest.writeString(focalLength);
        dest.writeString(iso);
        dest.writeString(exposureTime);
        dest.writeString(whiteBalance);
        dest.writeString(meteringMode);
        dest.writeString(exposureMode);
    }

    public CharSequence[] toArray(){
        return new CharSequence[]{camera, resolution, aperture, focalLength, iso, exposureTime, whiteBalance, meteringMode, exposureMode};
    }

    public int[] getIcons(){
        return new int[]{
                R.drawable.camera,
                R.drawable.resolution,
                R.drawable.diaphragm};
    }

    public String[] getEXIFTitles(Context context){
        return new String[] {
                context.getResources().getString(R.string.camera),
                context.getResources().getString(R.string.resolution),
                context.getResources().getString(R.string.aperture),
                context.getResources().getString(R.string.focal_length),
                context.getResources().getString(R.string.iso),
                context.getResources().getString(R.string.exposure_time),
                context.getResources().getString(R.string.white_balance),
                context.getResources().getString(R.string.metering_mode),
                context.getResources().getString(R.string.exposure)
        };
    }
}
