package com.beautiful_wallpapers_hd_qhd.core.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.License;
import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;

/**
 * Created by Igor on 13.04.2016.
 */
public class Author implements Parcelable{

    private String nsid;
    private String realName;
    private String userName;
    private String userAvatar;
    private String location;
    private int licenseNumber;

    public Author(){

    }

    public Author(String nsid, String realName, String userName, String userAvatar, int licenseNumber){
        this.nsid = nsid;
        this.realName = realName;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.licenseNumber = licenseNumber;
    }

    protected Author(Parcel in) {
        nsid = in.readString();
        realName = in.readString();
        userName = in.readString();
        location = in.readString();
        licenseNumber = in.readInt();
    }

    public static final Creator<Author> CREATOR = new Creator<Author>() {
        @Override
        public Author createFromParcel(Parcel in) {
            return new Author(in);
        }

        @Override
        public Author[] newArray(int size) {
            return new Author[size];
        }
    };

    public String getNsid() {
        return nsid;
    }

    public void setNsid(String nsid) {
        this.nsid = nsid;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(int licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public CharSequence[] toArray(){
        return new CharSequence[]{
                (!realName.equals("") ? realName + " [" + userName + "]" : userName),
                "http://flickr.com/" + nsid,
                "See other photos",
                !location.equals("") ? location : "No information",
                "Write to " + (!realName.equals("") ? realName : userName),
                new License(licenseNumber).getLicenseName()};
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nsid);
        dest.writeString(realName);
        dest.writeString(userName);
        dest.writeString(location);
        dest.writeInt(licenseNumber);
    }

    public int[] getIcons(){
         return new int[] {
                R.drawable.ic_account,
                R.drawable.ic_flickr_launcher,
                R.drawable.ic_portfolio,
                R.drawable.ic_map_marker,
                R.drawable.ic_email,
                R.drawable.ic_copyright};
    }
}
