package com.beautiful_wallpapers_hd_qhd.core.database.cursor;

import android.database.Cursor;
import android.database.SQLException;

/**
 * Created by Igor on 13.04.2016.
 */
public class CursorReader {

    public void read(Cursor c, ICursorCloser iCursor){
        try {
            iCursor.onRead(c);
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            closeCursor(c);
        }

    }

    public void closeCursor(Cursor c){
        if(c != null){
            c.close();
        }
    }

}
