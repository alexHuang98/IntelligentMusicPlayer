package com.intelligentmusicplayer.android.musicplaying;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.intelligentmusicplayer.android.R;

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

    private MusicBinder mBinder = new MusicBinder();

    public class MusicBinder extends Binder{

        public void play(int playType){
            if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
                if(playType!=current_playing){
                    progress[current_playing] = mediaPlayer.getCurrentPosition();
                    current_playing = playType;
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
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
                    mediaPlayer = null;
                    initMediaPlayer(lastId[playType]);
                }
                mediaPlayer.seekTo(progress[playType]);
                mediaPlayer.start();
            }
            else{
                Toast.makeText(MusicPlayingService.this,
                        "MediaPlayer is null",Toast.LENGTH_SHORT).show();
                initMediaPlayer(lastId[playType]);
                mediaPlayer.start();
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
    public boolean onUnbind(Intent intent) {
        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //返回START_STICKY ：在运行onStartCommand后service进程被kill后，那将保留在开始状态，但是不保留那些传入的intent。
        // 不久后service就会再次尝试重新创建，因为保留在开始状态，在创建service后将保证调用onstartCommand。
        // 如果没有传递任何开始命令给service，那将获取到null的intent。
        return START_STICKY;
    }
}
