package com.massivcode.androidmusicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.massivcode.androidmusicplayer.database.DbHelper;
import com.massivcode.androidmusicplayer.database.MyPlaylistContract;

import java.util.ArrayList;


public class DataBackupUtil {

    private static final String TAG = DataBackupUtil.class.getSimpleName();
    private static DataBackupUtil sInstance = null;
    private static Context mContext = null;
    private static SharedPreferences mSharedPreferences = null;
    private static final String PREFERENCES_NAME = "LastPlayedSongs";

    public static String[] projection
            = new String[]{MyPlaylistContract.MyPlaylistEntry._ID,
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST,
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID};

    public static String selection = MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST + "=?";
    public static String[] selectionArgs = new String[]{MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_LAST_PLAYED};

    private DbHelper mDbHelper;

    private DataBackupUtil() {
    }


    public static synchronized DataBackupUtil getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DataBackupUtil();
            mContext = context;
            mSharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
        return sInstance;
    }

    public void saveIsShuffle(boolean isShuffle) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("shuffle", isShuffle).commit();
    }

    public boolean loadIsShuffle() {
        boolean result = mSharedPreferences.getBoolean("shuffle", false);
        return result;
    }

    public void saveIsRepeat(boolean isRepeat) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("repeat", isRepeat).commit();
    }

    public boolean loadIsRepeat() {
        boolean result = mSharedPreferences.getBoolean("repeat", false);
        return result;
    }

    public void saveCurrentPlayingMusicPosition(int position) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("position", position).commit();
    }


    public int loadCurrentPlayingMusicPosition() {
        int position = mSharedPreferences.getInt("position", -1);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove("position").commit();

        return position;
    }


    public void saveCurrentPlaylist(ArrayList<Long> currentPlaylist) {
        mDbHelper = DbHelper.getInstance(mContext);
        SQLiteDatabase db = null;
        SQLiteStatement statement;


        try {
            if (currentPlaylist != null && currentPlaylist.size() > 0) {
                db = mDbHelper.getWritableDatabase();
                db.beginTransaction();

                statement = db.compileStatement(

                        "INSERT INTO " + MyPlaylistContract.MyPlaylistEntry.TABLE_NAME + " ( " +
                                MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST + " , " +
                                MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID + " , " +
                                MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST_TYPE + " ) " +
                                "values(?, ?, ?)"
                );

                for (long id : currentPlaylist) {
                    int column = 1;
                    statement.bindString(column++, MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_LAST_PLAYED);
                    statement.bindLong(column++, id);
                    statement.bindString(column++, MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_LAST_PLAYED);
                    statement.execute();
                }

                statement.close();
                db.setTransactionSuccessful();
            }


        } catch (RuntimeException e) {

            e.printStackTrace();

        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }

    }


    public ArrayList<Long> loadLastPlayedSongs() {
        mDbHelper = DbHelper.getInstance(mContext);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.query(true, MyPlaylistContract.MyPlaylistEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null, null);

        ArrayList<Long> lastPlayedSongs = new ArrayList<>();

        while (cursor.moveToNext()) {
            lastPlayedSongs.add(cursor.getLong(cursor.getColumnIndexOrThrow(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID)));
        }

        cursor.close();

        db.delete(MyPlaylistContract.MyPlaylistEntry.TABLE_NAME, selection, selectionArgs);


        return lastPlayedSongs;
    }


}
