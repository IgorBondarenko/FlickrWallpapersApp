package com.beautiful_wallpapers_hd_qhd.core.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.beautiful_wallpapers_hd_qhd.core.database.cursor.CursorReader;
import com.beautiful_wallpapers_hd_qhd.core.database.cursor.ICursorCloser;
import com.beautiful_wallpapers_hd_qhd.core.entity.Author;
import com.beautiful_wallpapers_hd_qhd.core.entity.FlickrImage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Igor on 13.04.2016.
 */
public class FlickrDatabase {

    private FlickrDataBaseHelper dbHelper;
    private SQLiteDatabase dataBase;

    public FlickrDatabase(Context context){
        this.dbHelper = FlickrDataBaseHelper.getInstance(context.getApplicationContext());
        this.dataBase = dbHelper.getWritableDatabase();
    }

    public void addPhoto(String flickrId, String table, String value){
        ContentValues cv = new ContentValues();
        cv.put(FlickrDataBaseHelper.TABLE_PHOTO_FLICKR_ID, flickrId);
        switch (table){
            case FlickrDataBaseHelper.TABLE_THUMB_SIZE:
                cv.put(FlickrDataBaseHelper.TABLE_PHOTO_THUMB_URL, value);
                break;
            case FlickrDataBaseHelper.TABLE_PREVIEW_SIZE:
                cv.put(FlickrDataBaseHelper.TABLE_PHOTO_PREVIEW_URL, value);
                break;
            case FlickrDataBaseHelper.TABLE_ORIGINAL_SIZE:
                cv.put(FlickrDataBaseHelper.TABLE_PHOTO_ORIGINAL_URL, value);
                break;
        }
        dataBase.insertWithOnConflict(table, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void addPhotos(List<FlickrImage> flickrImages, String table){
        for (FlickrImage image : flickrImages) {
            addPhoto(image.getFlickrId(), table, image.getSomeSizeUrl());
        }
    }

    public String getPhoto(String table, String flickrId){
        Cursor c = null;
        try{
            c = dataBase.rawQuery("SELECT * FROM " + table + " WHERE " + FlickrDataBaseHelper.TABLE_PHOTO_FLICKR_ID + " = ?", new String[]{flickrId});
            if(c.moveToNext()){
                return createPhotoObject(c, table).getSomeSizeUrl();
            }
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            new CursorReader().closeCursor(c);
        }
        return null;
    }

    public List<FlickrImage> getPhotos(final String table){
        final List<FlickrImage> flickrImages = new ArrayList<>();
        Cursor c = dataBase.rawQuery("SELECT * FROM ?", new String[]{table});
        new CursorReader().read(c, new ICursorCloser() {
            @Override
            public void onRead(Cursor c) {
                while (c.moveToNext()){
                    flickrImages.add(createPhotoObject(c, table));
                }
            }
        });
        return flickrImages;
    }

    public static final int FAVOURITE_PHOTO = 0;
    public static final int FAVOURITE_AUTHOR = 1;

    public void addFavourite(String flickrId, int favouriteType){
        ContentValues cv = new ContentValues();

        switch (favouriteType){
            case FAVOURITE_PHOTO:
                cv.put(FlickrDataBaseHelper.TABLE_PHOTO_FLICKR_ID, flickrId);
                dataBase.insert(FlickrDataBaseHelper.TABLE_FAVOURITE_PHOTOS, null, cv);
                break;
            case FAVOURITE_AUTHOR:
                cv.put(FlickrDataBaseHelper.TABLE_AUTHOR_NSID, flickrId);
                dataBase.insert(FlickrDataBaseHelper.TABLE_FAVOURITE_AUTHORS, null, cv);
                break;
        }
    }

    public boolean isFavourite(String flickrId, int favouriteType){
        Cursor c = null;
        try {
            switch (favouriteType){
                case FAVOURITE_PHOTO:
                    c = dataBase.rawQuery("SELECT * FROM " + FlickrDataBaseHelper.TABLE_FAVOURITE_PHOTOS + " WHERE " + FlickrDataBaseHelper.TABLE_PHOTO_FLICKR_ID + " = ?", new String[]{flickrId});
                    break;
                case FAVOURITE_AUTHOR:
                    c = dataBase.rawQuery("SELECT * FROM " + FlickrDataBaseHelper.TABLE_FAVOURITE_AUTHORS + " WHERE " + FlickrDataBaseHelper.TABLE_AUTHOR_NSID + " = ?", new String[]{flickrId});
                    break;
            }
            return c.moveToNext();
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            new CursorReader().closeCursor(c);
        }
        return false;
    }

    public List<String> getFavourites(int favouriteType){
        List<String> favourite = new ArrayList<>();
        Cursor c = null;
        try {
            switch (favouriteType){
                case FAVOURITE_PHOTO:
                    c = dataBase.rawQuery("SELECT * FROM " + FlickrDataBaseHelper.TABLE_FAVOURITE_PHOTOS, null);
                    while (c.moveToNext()){
                        favourite.add(c.getString(c.getColumnIndex(FlickrDataBaseHelper.TABLE_PHOTO_FLICKR_ID)));
                    }
                    break;
                case FAVOURITE_AUTHOR:
                    c = dataBase.rawQuery("SELECT * FROM " + FlickrDataBaseHelper.TABLE_FAVOURITE_AUTHORS, null);
                    while (c.moveToNext()){
                        favourite.add(c.getString(c.getColumnIndex(FlickrDataBaseHelper.TABLE_AUTHOR_NSID)));
                    }
                    break;
            }
            return favourite;
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            new CursorReader().closeCursor(c);
        }
        return favourite;
    }

    public void removeFavourite(String flickrId, int favouriteType){
        switch (favouriteType){
            case FAVOURITE_PHOTO:
                dataBase.delete(FlickrDataBaseHelper.TABLE_FAVOURITE_PHOTOS, FlickrDataBaseHelper.TABLE_PHOTO_FLICKR_ID + " = ?", new String[]{flickrId});
                break;
            case FAVOURITE_AUTHOR:
                dataBase.delete(FlickrDataBaseHelper.TABLE_FAVOURITE_AUTHORS, FlickrDataBaseHelper.TABLE_AUTHOR_NSID + " = ?", new String[]{flickrId});
                break;
        }
    }

    public void addAuthor(Author author){
        ContentValues cv = new ContentValues();
        cv.put(FlickrDataBaseHelper.TABLE_AUTHOR_NSID, author.getNsid());
        cv.put(FlickrDataBaseHelper.TABLE_AUTHOR_REALNAME, author.getRealName());
        cv.put(FlickrDataBaseHelper.TABLE_AUTHOR_USERNAME, author.getUserName());
        cv.put(FlickrDataBaseHelper.TABLE_AUTHOR_AVATAR, author.getUserAvatar());
        dataBase.insert(FlickrDataBaseHelper.TABLE_AUTHORS, null, cv);
    }

    public Author getAuthor(String flickrAuthorNsid){
        Cursor c = null;
        try {
            c = dataBase.rawQuery("SELECT * FROM " + FlickrDataBaseHelper.TABLE_AUTHORS + " WHERE " + FlickrDataBaseHelper.TABLE_AUTHOR_NSID + " = ?", new String[]{flickrAuthorNsid});
            if(c.moveToNext()){
                return createAuthorObject(c);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            new CursorReader().closeCursor(c);
        }
        return null;
    }

    public List<Author> getAuthors(){
        final List<Author> authors = new ArrayList<>();
        Cursor c = dataBase.rawQuery("SELECT * FROM ?", new String[]{FlickrDataBaseHelper.TABLE_AUTHORS});
        new CursorReader().read(c, new ICursorCloser() {
            @Override
            public void onRead(Cursor c) {
                while (c.moveToNext()){
                    authors.add(createAuthorObject(c));
                }
            }
        });
        return authors;
    }

    public void removeAuthor(String flickrAuthorNsid){
        dataBase.delete(FlickrDataBaseHelper.TABLE_AUTHORS, FlickrDataBaseHelper.TABLE_AUTHOR_NSID + " = ?", new String[]{flickrAuthorNsid});
    }

    private FlickrImage createPhotoObject(Cursor c, String table){
        FlickrImage image = new FlickrImage();
        image.setFlickrId(c.getString(c.getColumnIndex(FlickrDataBaseHelper.TABLE_PHOTO_FLICKR_ID)));
        switch (table){
            case FlickrDataBaseHelper.TABLE_THUMB_SIZE:
                image.setSomeSizeUrl(c.getString(c.getColumnIndex(FlickrDataBaseHelper.TABLE_PHOTO_THUMB_URL)));
                break;
            case FlickrDataBaseHelper.TABLE_PREVIEW_SIZE:
                image.setSomeSizeUrl(c.getString(c.getColumnIndex(FlickrDataBaseHelper.TABLE_PHOTO_PREVIEW_URL)));
                break;
            case FlickrDataBaseHelper.TABLE_ORIGINAL_SIZE:
                image.setSomeSizeUrl(c.getString(c.getColumnIndex(FlickrDataBaseHelper.TABLE_PHOTO_ORIGINAL_URL)));
                break;
        }
        return image;
    }

    private Author createAuthorObject(Cursor c){
        Author author = new Author();
        author.setNsid(c.getString(c.getColumnIndex(FlickrDataBaseHelper.TABLE_AUTHOR_NSID)));
        author.setRealName(c.getString(c.getColumnIndex(FlickrDataBaseHelper.TABLE_AUTHOR_REALNAME)));
        author.setUserName(c.getString(c.getColumnIndex(FlickrDataBaseHelper.TABLE_AUTHOR_USERNAME)));
        author.setUserAvatar(c.getString(c.getColumnIndex(FlickrDataBaseHelper.TABLE_AUTHOR_AVATAR)));
        return author;
    }

    public void closeConnection(){
        dataBase.close();
    }

}
