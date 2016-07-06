package com.beautiful_wallpapers_hd_qhd.core;

/**
 * Created by Igor on 18.04.2016.
 */
public class License {

    private int licenseNumber = 0;

    public License(int licenseNumber){
        this.licenseNumber = licenseNumber;
    }

    public String getLicenseName(){
        switch (licenseNumber){
            case 0:
                return "All Rights Reserved";
            case 1:
                return "Attribution-NonCommercial-ShareAlike License";
            case 2:
                return "Attribution-NonCommercial License";
            case 3:
                return "Attribution-NonCommercial-NoDerivs License";
            case 4:
                return "Attribution License";
            case  5:
                return "Attribution-ShareAlike License";
            case 6:
                return "Attribution-NoDerivs License";
            case 7:
                return "No known copyright restrictions";
            case 8:
                return "United States Government Work";
        }
        return null;
    }

    public String getUrl(){
        switch (licenseNumber){
            case 0:
                return null;
            case 1:
                return "http://creativecommons.org/licenses/by-nc-sa/2.0/";
            case 2:
                return "http://creativecommons.org/licenses/by-nc/2.0/";
            case 3:
                return "http://creativecommons.org/licenses/by-nc-nd/2.0/";
            case 4:
                return "http://creativecommons.org/licenses/by/2.0/";
            case 5:
                return "http://creativecommons.org/licenses/by-sa/2.0/";
            case 6:
                return "http://creativecommons.org/licenses/by-nd/2.0/";
            case 7:
                return "http://flickr.com/commons/usage/";
            case 8:
                return "http://www.usa.gov/copyright.shtml";
        }
        return null;
    }

}
