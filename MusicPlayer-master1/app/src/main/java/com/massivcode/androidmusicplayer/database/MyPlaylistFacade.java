
package com.massivcode.androidmusicplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;


public class MyPlaylistFacade {
    private static final String TAG = MyPlaylistFacade.class.getSimpleName();
    private DbHelper mHelper;
    private Context mContext;

    public static String[] projection
            = new String[]{MyPlaylistContract.MyPlaylistEntry._ID,
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST,
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID,
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST_TYPE};

    public static String selection_music_id = MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID + "=?";
    public static String selection_playlist_name = MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST + "=?";
    public static String selection_playlist_type = MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST_TYPE + "=?";
    public static String selection_playlist_type_all = MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST_TYPE + "=? OR " +
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST_TYPE + "=?";
    public static String selection_playlist_type_and_name ="( " +  MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST_TYPE + " =? OR " +
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST_TYPE + " =? ) AND " +
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST + "=?";

    public static String selection_toggle_favorite = MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST_TYPE + " = ? and " +
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID + " = ? ";

    private static String getAllUserPlaylist_SQL = "select _id, playlist_name, _id, (select count(music_id) from MyPlaylist as b where b.playlist_name = MyPlaylist.playlist_name) as music_count from MyPlaylist group by playlist_name order by _id asc";
    private static String getChildrenPlaylist_SQL = "select _id, music_id from MyPlaylist where playlist_name = '";
    private static String getMusicIdsFromSelectedPlaylist_SQL = "select music_id from MyPlaylist where playlist_name = '";


    public MyPlaylistFacade(Context context) {
        mHelper = DbHelper.getInstance(context);
        mContext = context;
    }

    public void createDb() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        mHelper.onCreate(db);
    }


    public void deleteUserPlaylist(String name) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(MyPlaylistContract.MyPlaylistEntry.TABLE_NAME, MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST + "=?", new String[]{name});
    }


    public boolean isFavoritted(long musicId) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        boolean result = false;
        Cursor cursor = db.query(MyPlaylistContract.MyPlaylistEntry.TABLE_NAME, projection, selection_toggle_favorite, new String[]{MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_FAVORITE, String.valueOf(musicId)}, null, null, null);

        // 기존에 이런 데이터가 있을 때 -> true
        if(cursor != null && cursor.getCount() != 0) {
            result = true;
        }

        cursor.close();

        return result;
    }

    public void toggleFavoriteList(long musicId) {
        Log.d(TAG, "뮤직아이디 : " + musicId);
        SQLiteDatabase db = mHelper.getWritableDatabase();

        Cursor cursor = db.query(MyPlaylistContract.MyPlaylistEntry.TABLE_NAME, projection, selection_toggle_favorite, new String[]{MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_FAVORITE, String.valueOf(musicId)}, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST, "즐겨찾기");
            values.put(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID, musicId);
            values.put(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST_TYPE, MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_FAVORITE);
            db.insert(MyPlaylistContract.MyPlaylistEntry.TABLE_NAME, null, values);
            Log.d(TAG, "즐겨찾기에 " + musicId + " 를 추가하였습니다.");
        }
        else {
            db.delete(MyPlaylistContract.MyPlaylistEntry.TABLE_NAME, selection_toggle_favorite, new String[]{MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_FAVORITE, String.valueOf(musicId)});
            Log.d(TAG, "즐겨찾기에서 " + musicId + " 를 제거하였습니다.");
        }

        if (cursor != null) {
            cursor.close();
        }
    }


    public Cursor getChildrenCursor(String playlist_name) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        return db.rawQuery(getChildrenPlaylist_SQL + playlist_name + "'", null);
    }


    public Cursor getAllUserPlaylist() {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        return db.rawQuery(getAllUserPlaylist_SQL, null);
    }

    public boolean isAlreadyExist() {
        boolean result = true;

        Cursor cursor = getAllUserPlaylist();

        if(cursor == null || cursor.getCount() == 0) {
            result = false;
        }

        if (cursor != null) {
            cursor.close();
        }

        return result;
    }

    public Cursor getSelectedPlaylistMusicIds(String userPlaylistName) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        return db.rawQuery(getMusicIdsFromSelectedPlaylist_SQL + userPlaylistName + "'", null);
    }



}
