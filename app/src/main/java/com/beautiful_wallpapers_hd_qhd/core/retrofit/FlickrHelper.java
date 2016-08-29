package com.beautiful_wallpapers_hd_qhd.core.retrofit;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.HttpGet;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.HttpClient;

/**
 * Created by Igor on 16.12.2015.
 */
public class FlickrHelper {

    public static final String API_KEY = "92a9755567b8b05485bf1605c421997e";
    public static final String GROUP_ID = "2781725@N20";
    public static final String RETRO_URL = "https://api.flickr.com/";
    public static final String FLICKR_USER_HP = "https://flickr.com/people/";
    public static final String FLICKR_USER_SEND_MAIL = "https://www.flickr.com/mail/write/?to=";

    //methods
    public static final String METHOD_GET_PHOTOS_BY_GROUP = "flickr.groups.pools.getPhotos";
    public static final String METHOD_GET_PHOTO_SIZES = "flickr.photos.getSizes";
    public static final String METHOD_GET_EXIF = "flickr.photos.getExif";
    public static final String METHOD_PHOTOS_GET_INFO = "flickr.photos.getInfo";
    public static final String METHOD_PEOPLE_GET_INFO = "flickr.people.getInfo";
    public static final String METHOD_SEARCH = "flickr.photos.search";

    //arguments
    public static final String ARG_GET_PHOTO_ID = "photo_id";
    public static final String ARG_GET_GROUP_ID = "group_id";
    public static final String ARG_USER_ID = "user_id";
    public static final String ARG_TEXT = "text";
    public static final String ARG_TAGS = "tags";
    public static final String ARG_METHOD = "method";

    //image sizes
    public static final int SIZE_SQUARE = 0;
    public static final int SIZE_LARGE_SQUARE = 1;
    public static final int SIZE_THUMBNAIL = 2;
    public static final int SIZE_SMALL = 3;
    public static final int SIZE_SMALL_320 = 4;
    public static final int SIZE_MEDIUM = 5;
    public static final int SIZE_MEDIUM_640 = 6;
    public static final int SIZE_MEDIUM_800 = 7;
    public static final int SIZE_LARGE = 8;
    public static final int SIZE_ORIGINAL = 11;

    //exif
    public static final String EXIF_CAMERA = "Make";
    public static final String EXIF_CAMERA_MODEL = "Model";
    public static final String EXIF_ISO = "ISO";
    public static final String EXIF_FOCAL_LENGTH = "FocalLength"; //Фокусное расстояние
    public static final String EXIF_EXPOSURE_TIME = "ExposureTime"; //Выдержка
    public static final String EXIF_EXPOSURE_MODE = "ExposureMode"; //Экспозиция
    public static final String EXIF_WHITE_BALANCE = "WhiteBalance";
    public static final String EXIF_METERING_MODE = "MeteringMode"; //Экспозамер
    public static final String EXIF_APERTURE = "FNumber"; //Апертура

    public static String getUserAvatar(String farm, String server, String nsid){
        return String.format("http://farm%s.staticflickr.com/%s/buddyicons/%s_l.jpg", new String[]{farm, server, nsid});
    }
}
