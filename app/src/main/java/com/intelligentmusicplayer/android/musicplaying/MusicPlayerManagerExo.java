package com.intelligentmusicplayer.android.musicplaying;

import android.util.Log;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.intelligentmusicplayer.android.MyApplication;

import java.util.HashMap;
import java.util.Map;

public class MusicPlayerManagerExo extends MusicPlayerManagerInterface {

    private static final String TAG = "MusicHPY";

    private MusicDataProvider musicDataProvider;

    private SimpleExoPlayer player;

    private MotionState currentState = null;

    private Map<MotionState,MusicPlayingState> stateRecord;

    public MusicPlayerManagerExo() {
//        musicDataProvider = new MusicDataProvider();
        musicDataProvider = MusicDataProvider.getInstance();
        player = new SimpleExoPlayer.Builder(MyApplication.getContext()).build();
        stateRecord = new HashMap<>();
        for(MotionState state:MotionState.values()){
            stateRecord.put(state,new MusicPlayingState(0,(long)0));
        }
    }

    @Override
    public void play(MotionState playType) {

        if(currentState==null){
            loadState(playType);
        }
        else if(currentState!=playType){
            saveState();
            loadState(playType);
        }
        currentState = playType;
        player.setPlayWhenReady(true);


    }

    @Override
    public void pause() {
        player.setPlayWhenReady(false);
        saveState();
    }

    @Override
    public void release() {
        player.release();
    }

    private void saveState(){
        stateRecord.get(currentState).setState(player.getCurrentWindowIndex(),player.getCurrentPosition());
    }

    private void loadState(MotionState state){
        player.prepare(musicDataProvider.getMediaSource(state));
        MusicPlayingState playingState = stateRecord.get(state);
        player.seekTo(playingState.getWindowIndex(),playingState.getPosition());
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
    }
}
