package com.intelligentmusicplayer.android.musicplaying;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.intelligentmusicplayer.android.MyApplication;
import com.intelligentmusicplayer.android.R;

import java.util.HashMap;
import java.util.Map;

public class MusicPlayerManager extends MusicPlayerManagerInterface{


    private Map<MotionState,Integer> progress;

    private Map<MotionState, Integer> lastId;

    private MotionState current_state;

    private MediaPlayer mediaPlayer;


    public MusicPlayerManager(){

        progress = new HashMap<>();
        lastId = new HashMap<>();
        for(MotionState ms:MotionState.values()){
            progress.put(ms,0);
        }
        lastId.put(MotionState.SLOW, R.raw.slow);
        lastId.put(MotionState.NORMAL, R.raw.normal);
        lastId.put(MotionState.FAST, R.raw.fast);

    }

    private void initMediaPlayer(int id){
        SimpleExoPlayer player = new SimpleExoPlayer.Builder(MyApplication.getContext()).build();

        try{
            mediaPlayer = MediaPlayer.create(MyApplication.getContext(),id);
            mediaPlayer.prepare();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void play(MotionState playType){

        try{
            if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
                if(playType!=current_state){
                    progress.put(current_state,mediaPlayer.getCurrentPosition());
                    current_state = playType;
                    release();
                    initMediaPlayer(lastId.get(playType));
                    mediaPlayer.seekTo(progress.get(playType));
                    mediaPlayer.start();
                }
            }
            else if(mediaPlayer!=null&&!mediaPlayer.isPlaying()){
                if(current_state!=playType)
                {
                    release();
                    initMediaPlayer(lastId.get(playType));
                }
                mediaPlayer.seekTo(progress.get(playType));
                mediaPlayer.start();
            }
            else{
                current_state = playType;
                initMediaPlayer(lastId.get(playType));
                mediaPlayer.start();

            }
        }
        catch (NullPointerException e){
            e.printStackTrace();

        }

    }

    public void pause(){
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            progress.put(current_state,mediaPlayer.getCurrentPosition());
            mediaPlayer.pause();
        }
    }

    public void release(){
        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
