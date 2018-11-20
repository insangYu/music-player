
package com.massivcode.androidmusicplayer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.events.MusicEvent;
import com.massivcode.androidmusicplayer.events.PlayBack;
import com.suwonsmartapp.abl.AsyncBitmapLoader;


public class SongAdapter extends CursorAdapter implements AsyncBitmapLoader.BitmapLoadListener {

    private static final String TAG = SongAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private AsyncBitmapLoader mAsyncBitmapLoader;
    private Context mContext;

    private int mCurrentPlayingPosition;

    private MusicEvent mMusicEvent;
    private PlayBack mPlayback;

    public SongAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);

        mInflater = LayoutInflater.from(context);
        mContext = context;
        mAsyncBitmapLoader = new AsyncBitmapLoader(context);
        mAsyncBitmapLoader.setBitmapLoadListener(this);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();

        View view = mInflater.inflate(R.layout.item_songs, parent, false);
        holder.AlbumArtImageView = (ImageView)view.findViewById(R.id.item_songs_album_iv);
        holder.TitleTextView = (TextView)view.findViewById(R.id.item_songs_title_tv);
        holder.ArtistTextView = (TextView)view.findViewById(R.id.item_songs_artist_tv);
        holder.IsPlayImageView = (ImageView)view.findViewById(R.id.item_songs_isPlay_iv);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder)view.getTag();
        int id = (int) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        holder.TitleTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
        holder.ArtistTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));

        mAsyncBitmapLoader.loadBitmap(cursor.getPosition(), holder.AlbumArtImageView);


        if (mMusicEvent != null && mPlayback != null) {

            if(mMusicEvent.getMusicInfo() != null) {
                if (id == mMusicEvent.getMusicInfo().get_id()) {
                    holder.IsPlayImageView.setVisibility(View.VISIBLE);
                    mCurrentPlayingPosition = cursor.getPosition();
                    if (mPlayback.isPlaying()) {
                        holder.IsPlayImageView.setSelected(true);
                    } else {
                        holder.IsPlayImageView.setSelected(false);
                    }

                } else {
                    holder.IsPlayImageView.setVisibility(View.GONE);
                }
            }

        }


    }


    public void swapMusicEvent(MusicEvent musicEvent) {
        mMusicEvent = musicEvent;
    }

    public void swapPlayback(PlayBack playback) {
        mPlayback = playback;
    }

    @Override
    public Bitmap getBitmap(int position) {

        long id = getItemId(position);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mContext, Uri.parse("content://media/external/audio/media/" + id));

        byte[] albumArt =  retriever.getEmbeddedPicture();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap;
        if (null != albumArt) {
            bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length, options);
        } else {
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), android.R.drawable.ic_menu_report_image);
        }

        return bitmap;
    }

    static class ViewHolder {
        ImageView AlbumArtImageView;
        TextView TitleTextView, ArtistTextView;
        ImageView IsPlayImageView;
    }
}
