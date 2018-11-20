

package com.massivcode.androidmusicplayer.managers;

import android.support.v4.app.Fragment;

import com.massivcode.androidmusicplayer.fragments.PlayerFragment;
import com.massivcode.androidmusicplayer.fragments.PlaylistFragment;
import com.massivcode.androidmusicplayer.fragments.SongsFragment;


public class Manager {

    public static final Class[] FRAGMENTS = new Class[] {
            PlayerFragment.class,
            PlaylistFragment.class,
            SongsFragment.class
    };

    private Manager() {}

    public static Fragment getInstance(int position) {
        try {
            return (Fragment)FRAGMENTS[position].newInstance();
        } catch (Exception e) {
            return null;
        }
    }

}