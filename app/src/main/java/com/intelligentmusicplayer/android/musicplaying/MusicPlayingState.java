package com.intelligentmusicplayer.android.musicplaying;

public class MusicPlayingState {
    private int windowIndex;
    private Long position;

    public MusicPlayingState(int windowIndex,Long position){
        this.windowIndex = windowIndex;
        this.position = position;
    }

    int getWindowIndex(){
        return windowIndex;
    }
    Long getPosition(){
        return position;
    }

    void setState(int windowIndex,Long position){
        this.windowIndex = windowIndex;
        this.position = position;
    }
}
