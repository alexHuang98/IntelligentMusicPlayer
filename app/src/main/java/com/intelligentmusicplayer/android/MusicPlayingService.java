package com.intelligentmusicplayer.android;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MusicPlayingService extends Service {

    public static final int SLOW =0;

    public static final int NORMAL =1;

    public static final int FAST =2;

    private int[] progress = new int[3];

    private int[] lastId = new int[3];


    private int current_playing;

    private MediaPlayer mediaPlayer;

    public MusicPlayingService() {

    }

    private void initMediaPlayer(int id){
        try{
            mediaPlayer = MediaPlayer.create(this,id);
            mediaPlayer.prepare();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private MusicBinder mbinder = new MusicBinder();

    class MusicBinder extends Binder{

        public void play(int playType){
            if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
                if(playType!=current_playing){
                    progress[current_playing] = mediaPlayer.getCurrentPosition();
                    current_playing = playType;
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    initMediaPlayer(lastId[playType]);
                    mediaPlayer.seekTo(progress[playType]);
                    mediaPlayer.start();
                }
            }
            else if(mediaPlayer!=null&&!mediaPlayer.isPlaying()){
                if(current_playing!=playType)
                {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    initMediaPlayer(lastId[playType]);
                }
                mediaPlayer.seekTo(progress[playType]);
                mediaPlayer.start();
            }
            else{
                Toast.makeText(MusicPlayingService.this,
                        "MediaPlayer is null",Toast.LENGTH_SHORT).show();
            }
        }

        public void pause(){
            if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
                progress[current_playing] = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        initMediaPlayer(R.raw.slow);
        int[] typeToId = {R.raw.slow,R.raw.normal,R.raw.fast};
        for(int i=0;i<3;i++)
        {
            progress[i]=0;
            lastId[i]=typeToId[i];
        }
        return mbinder;
    }
}
