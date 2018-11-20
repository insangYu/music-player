package com.massivcode.androidmusicplayer.events;

public class PlayBack implements Event {

    private boolean mPlaying;
    private int mCurrentTime;

    public boolean isPlaying() {
        return mPlaying;
    }

    public void setPlaying(boolean mPlaying) {
        this.mPlaying = mPlaying;
    }

    public int getCurrentTime() {
        return mCurrentTime;
    }

    public void setCurrentTime(int mCurrentTime) {
        this.mCurrentTime = mCurrentTime;
    }

}
