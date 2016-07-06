package com.beautiful_wallpapers_hd_qhd.core.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Igor on 16.12.2015.
 */
public class FlickrDataBaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "com.wallpapers_hd_qhd.flickr.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_PHOTO_FLICKR_ID = "flickr_image_id";

//    table thumb
    public static final String TABLE_THUMB_SIZE = "thumb_size_table";
    public static final String TABLE_PHOTO_THUMB_URL = "thumb_url";

//    table preview
    public static final String TABLE_PREVIEW_SIZE = "preview_size_table";
    public static final String TABLE_PHOTO_PREVIEW_URL = "preview_url";

//    table original
    public static final String TABLE_ORIGINAL_SIZE = "original_size_table";
    public static final String TABLE_PHOTO_ORIGINAL_URL = "preview_url";

//    table favourite images
    public static final String TABLE_FAVOURITE_PHOTOS = "favourite_photos_table";

    public static final String TABLE_AUTHOR_NSID = "flickr_author_nsid";

//    table authors
    public static final String TABLE_AUTHORS = "flickr_authors_table";
    public static final String TABLE_AUTHOR_REALNAME = "author_realname";
    public static final String TABLE_AUTHOR_USERNAME = "author_username";
    public static final String TABLE_AUTHOR_AVATAR = "author_avatar";

//    table favourite authors
    public static final String TABLE_FAVOURITE_AUTHORS = "favourite_authors_table";

    private static FlickrDataBaseHelper mInstance = null;
    public static synchronized FlickrDataBaseHelper getInstance(Context context){
        if(mInstance == null)
            mInstance = new FlickrDataBaseHelper(context);
        return mInstance;
    }

    private FlickrDataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE "+ TABLE_THUMB_SIZE + " ("
                + TABLE_PHOTO_FLICKR_ID + " TEXT PRIMARY KEY,"
                + TABLE_PHOTO_THUMB_URL + " TEXT"
                + ");");

        sqLiteDatabase.execSQL(
                "CREATE TABLE "+ TABLE_PREVIEW_SIZE + " ("
                        + TABLE_PHOTO_FLICKR_ID + " TEXT PRIMARY KEY,"
                        + TABLE_PHOTO_PREVIEW_URL + " TEXT"
                        + ");");

        sqLiteDatabase.execSQL(
                "CREATE TABLE "+ TABLE_ORIGINAL_SIZE + " ("
                        + TABLE_PHOTO_FLICKR_ID + " TEXT PRIMARY KEY,"
                        + TABLE_PHOTO_ORIGINAL_URL + " TEXT"
                        + ");");

        sqLiteDatabase.execSQL(
                "CREATE TABLE "+ TABLE_FAVOURITE_PHOTOS + " ("
                        + TABLE_PHOTO_FLICKR_ID + " TEXT PRIMARY KEY"
                        + ");");

        sqLiteDatabase.execSQL(
                "CREATE TABLE "+ TABLE_AUTHORS + " ("
                        + TABLE_AUTHOR_NSID + " TEXT PRIMARY KEY,"
                        + TABLE_AUTHOR_REALNAME + " TEXT,"
                        + TABLE_AUTHOR_USERNAME + " TEXT,"
                        + TABLE_AUTHOR_AVATAR + " TEXT"
                        + ");");

        sqLiteDatabase.execSQL(
                "CREATE TABLE "+ TABLE_FAVOURITE_AUTHORS + " ("
                        + TABLE_AUTHOR_NSID + " TEXT PRIMARY KEY"
                        + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
