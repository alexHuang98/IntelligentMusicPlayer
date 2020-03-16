package com.intelligentmusicplayer.android;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.intelligentmusicplayer.android.musicplaying.MusicPlayingService;
import com.intelligentmusicplayer.android.stepcount.StepCountService;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class MotionDetectorService extends Service {

    private static final String TAG = "MotionDetectorHPY";
    
    private MotionBinder mBinder = new MotionBinder();

    private MusicPlayingService.MusicBinder musicBinder;

    private StepCountService.StepBinder stepBinder;

    int currentType = MusicPlayingService.SLOW;

    public static final int TIMER = 0;

    private int timeScale = 60;

    private double normalSteps =1.7*timeScale; // 1.7 = 102/60

    private double fastSteps = 2.5 *timeScale;  //2.5 = 150/60

    private int currentSteps;

    private Runnable musicSwitcher;

    private UpdateUiCallBack mCallBack;

    private void MotionStateDecider(){
        currentSteps = stepBinder.getCurrentStep();
        stepBinder.refreshStep();
        if(currentSteps<normalSteps){
            currentType = MusicPlayingService.SLOW;
        }
        else if(currentSteps<fastSteps)
        {
            currentType = MusicPlayingService.NORMAL;
        }
        else{
            currentType = MusicPlayingService.FAST;
        }
//        Random random = new Random();
//        currentType = random.nextInt(3);

    }

    private Runnable getSwitcher(){
        return new Runnable() {
            @Override
            public void run() {
                MotionStateDecider();
                musicBinder.play(currentType);
                mCallBack.updateUi(currentType,currentSteps);
                handler.postDelayed(this,timeScale*1000);
            }
        };
    }

    private Handler handler = new Handler();

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MusicPlayingService.MusicBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private ServiceConnection stepConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            stepBinder = (StepCountService.StepBinder) service;
            Log.d(TAG, "onServiceConnected: success");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public MotionDetectorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Intent stepIntent = new Intent(this, StepCountService.class);
        Intent musicIntent = new Intent(this, MusicPlayingService.class);
        boolean stepConnected = bindService(stepIntent,stepConnection,BIND_AUTO_CREATE);
        boolean musicConnected = bindService(musicIntent,musicConnection,BIND_AUTO_CREATE);


        return mBinder;
    }

    public class MotionBinder extends Binder {
        void play(){
            stepBinder.refreshStep();
            musicBinder.play(currentType);
            if(musicSwitcher==null)
            {
                musicSwitcher = getSwitcher();
                handler.postDelayed(musicSwitcher,timeScale*1000);
            }
        }

        void pause(){
            musicBinder.pause();
            handler.removeCallbacks(musicSwitcher);
            musicSwitcher=null;
        }

        void setType(int type){
            if(type<3 && type>=0)
            {
                currentType = type;
                mCallBack.updateUi(type,-1);
            }

        }

        MotionDetectorService getService(){
            return MotionDetectorService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(musicConnection);
        unbindService(stepConnection);
    }

    void detectMotionState(){

    }

    public void registerCallback(UpdateUiCallBack mCallBack){
        this.mCallBack = mCallBack;
    }

}
