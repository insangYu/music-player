
package com.massivcode.androidmusicplayer.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.database.MyPlaylistContract;
import com.massivcode.androidmusicplayer.database.MyPlaylistFacade;
import com.massivcode.androidmusicplayer.models.MusicInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class MusicInfoLoadUtil {

    private static final String TAG = MusicInfoLoadUtil.class.getSimpleName();


    public static String[] projection = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION};

    public static String selection = "_id=?";
    public static String selection_artist = MediaStore.Audio.Media.ARTIST + "=?";


    public static Cursor search(Context context, String keyWord) {

        String where = MediaStore.Audio.Media.ARTIST + " != ? " + " AND " + MediaStore.Audio.Media.ARTIST + " like '%" + keyWord + "%' OR " + MediaStore.Audio.Media.TITLE + " like '%" + keyWord + "%'";
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, where, new String[]{MediaStore.UNKNOWN_STRING}, null);
    }

    public static HashMap<Long, MusicInfo> getAllMusicInfo(Context context) {
        HashMap<Long, MusicInfo> map = new HashMap<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.ARTIST + " != ? AND " + MediaStore.Audio.Media.TITLE + " NOT LIKE '%" + "hangout" + "%'" , new String[]{MediaStore.UNKNOWN_STRING}, null);


        if (cursor != null || cursor.getCount() != 0)
            while (cursor.moveToNext()) {

                long _id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                Uri uri = Uri.parse("content://media/external/audio/media/" + _id);
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                if (duration != null) {
                    MusicInfo musicInfo = new MusicInfo(_id, uri, artist, title, album, Integer.parseInt(duration));
                    map.put(_id, musicInfo);
                }

            }


        return map;
    }


    public static ArrayList<Long> getMusicIdListFromPlaylistName(String playlistName, Context context) {
        ArrayList<Long> list = new ArrayList<>();

        MyPlaylistFacade facade = new MyPlaylistFacade(context);
        Cursor cursor = facade.getSelectedPlaylistMusicIds(playlistName);

        while (cursor.moveToNext()) {
            long id = cursor.getInt(cursor.getColumnIndexOrThrow(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID));
            list.add(id);
        }

        cursor.close();
        return list;
    }


    public static String[] getArtistAndTitleFromId(Context context, int id) {
        String[] result = new String[2];

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media._ID + " = ?", new String[]{String.valueOf(id)}, null);
        if (cursor != null || cursor.getCount() != 0) {
            cursor.moveToFirst();
            result[0] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            result[1] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            cursor.close();
        }
        return result;
    }

    public static ArrayList<MusicInfo> getMusicInfoByIds(Context context, ArrayList<Long> ids) {
        ArrayList<MusicInfo> list = new ArrayList<>();

        for (Long id : ids) {
            MusicInfo musicInfo = new MusicInfo();
            Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media._ID + " = ?", new String[]{String.valueOf(id)}, null);
            if (cursor != null || cursor.getCount() != 0) {

                while (cursor.moveToNext()) {
                    long _id = id;
                    Uri uri = Uri.parse("content://media/external/audio/media/" + _id);
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                    musicInfo.set_id(_id);
                    musicInfo.setUri(uri);
                    musicInfo.setArtist(artist);
                    musicInfo.setTitle(title);
                    musicInfo.setAlbum(album);
                    if(duration != null) {
                        musicInfo.setDuration(Integer.parseInt(duration));
                    }
                    list.add(musicInfo);
                }
            }
        }

        return list;
    }

    public static ArrayList<Long> getIdListByMusicInfoList(ArrayList<MusicInfo> origin) {
        ArrayList<Long> list = new ArrayList<>();

        for (int i = 0; i < origin.size(); i++) {
            MusicInfo musicInfo = origin.get(i);
            list.add(musicInfo.get_id());
        }

        return list;

    }



    public static MusicInfo getSelectedMusicInfo(Context context, long id) {
        MusicInfo musicInfo = null;
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, new String[]{String.valueOf(id)}, null);
        if (cursor != null || cursor.getCount() != 0) {

            cursor.moveToFirst();
            long _id = id;
            Uri uri = Uri.parse("content://media/external/audio/media/" + _id);
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, uri);

            byte[] albumArt = retriever.getEmbeddedPicture();

            if(duration != null) {
                musicInfo = new MusicInfo(_id, uri, artist, title, album, albumArt, Integer.parseInt(duration));
            }

            cursor.close();
        }


        return musicInfo;


    }





    public static ArrayList<Long> getLastPlayedSongs(Set<String> values) {
        ArrayList<Long> list = new ArrayList<>();
        for (String value : values) {
            list.add(Long.getLong(value));
        }
        return list;
    }


    public static Set<String> getPlaylistToSet(ArrayList<Long> values) {
        Set<String> set = new HashSet<>();

        for (Long value : values) {
            set.add(String.valueOf(value));
        }

        return set;
    }


    public static ArrayList<Long> getSelectedSongPlaylist(Context context, Cursor cursor) {
        ArrayList<Long> list = new ArrayList<>();
        list.add(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
        return list;
    }



    public static ArrayList<Long> getPlayAllList(Context context) {
        ArrayList<Long> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.ARTIST + " != ? AND " + MediaStore.Audio.Media.TITLE + " NOT LIKE '%" + "hangout" + "%'", new String[]{MediaStore.UNKNOWN_STRING}, null);

        if(cursor != null || cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                list.add(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
            }

            cursor.close();
        }

        return list;
    }


    public static Bitmap getBitmap(Context context, Uri uri, int quality) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, uri);

        byte[] albumArt = retriever.getEmbeddedPicture();


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = quality;

        Bitmap bitmap;
        if (null != albumArt) {
            bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length, options);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_report_image);
        }


        return bitmap;
    }


    public static String getTime(String duration) {

        long milliSeconds = Long.parseLong(duration);
        int totalSeconds = (int) (milliSeconds / 1000);

        int hour = totalSeconds / 3600;
        int minute = (totalSeconds - (hour * 3600)) / 60;
        int second = (totalSeconds - ((hour * 3600) + (minute * 60)));


        return formattedTime(hour, minute, second);
    }


    private static String formattedTime(int hour, int minute, int second) {
        String result = "";

        if (hour > 0) {
            result = hour + ":";
        }

        if (minute >= 10) {
            result = result + minute + ":";
        } else {
            result = result + "0" + minute + ":";
        }

        if (second >= 10) {
            result = result + second;
        } else {
            result = result + "0" + second;
        }

        return result;
    }


}
