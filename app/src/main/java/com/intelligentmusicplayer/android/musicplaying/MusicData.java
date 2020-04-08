package com.intelligentmusicplayer.android.musicplaying;

import android.content.Context;
import android.media.MediaPlayer;

import com.google.android.exoplayer2.source.MediaSource;

public abstract class MusicData {
    private String title;
    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    MusicData(String title){
        this.title = title;
    }
    abstract MediaSource getMediaSource();

}
