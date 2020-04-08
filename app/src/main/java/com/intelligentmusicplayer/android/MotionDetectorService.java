package com.intelligentmusicplayer.android;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.intelligentmusicplayer.android.musicplaying.MusicPlayerManager;
import com.intelligentmusicplayer.android.musicplaying.MusicPlayerManagerExo;
import com.intelligentmusicplayer.android.musicplaying.MusicPlayerManagerInterface;
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

    // binders for related services
    private MusicPlayingService.MusicBinder musicBinder;
    private MusicPlayerManagerInterface musicPlayerManager;

    private StepCountService.StepBinder stepBinder;

    private HeartbeatService.HeartbeatBinder heartbeatBinder;

//    int currentType = MusicPlayingService.SLOW;
    MusicPlayerManager.MotionState currentType = MusicPlayerManager.MotionState.SLOW;

    private int timeScale = 5;

    private double normalSteps =1.7*timeScale; // 1.7 = 102/60
    private double fastSteps = 2.5 *timeScale;  //2.5 = 150/60

    private double normalHeartRate = 80;
    private double fastHeartRate = 120;

    private int currentSteps, currentHeartRates;

    private Runnable musicSwitcher;

    private UpdateUiCallBack mCallBack;

    private Intent stepIntent, musicIntent, heartIntent;

    public enum HeartrateDetectorState{
        SIMULATION, NONE, REAL
    }

    private HeartrateDetectorState heartrateDetectorState = HeartrateDetectorState.NONE;

    private void MotionStateDecider(){
        currentSteps = stepBinder.getCurrentStep();
        stepBinder.refreshStep();
        if(heartrateDetectorState==HeartrateDetectorState.NONE||heartbeatBinder==null)
        {
            if(currentSteps<normalSteps){
                currentType = MusicPlayerManager.MotionState.SLOW;
            }
            else if(currentSteps<fastSteps)
            {
                currentType = MusicPlayerManager.MotionState.NORMAL;
            }
            else{
                currentType = MusicPlayerManager.MotionState.FAST;

            }
            if(heartbeatBinder==null)
            {
                heartrateDetectorState =HeartrateDetectorState.NONE;
            }
        }
        else{
            currentHeartRates = heartbeatBinder.getHeartbeat();
            if(currentHeartRates<normalHeartRate){
                currentType = MusicPlayerManager.MotionState.SLOW;
            }
            else if(currentHeartRates<fastHeartRate)
            {
                currentType = MusicPlayerManager.MotionState.NORMAL;
            }
            else{
                currentType = MusicPlayerManager.MotionState.FAST;
            }
        }
//        Random random = new Random();
//        currentType = random.nextInt(3);

    }

    private Runnable getSwitcher(){
        return new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, heartrateDetectorState.toString());
                if(heartbeatBinder==null){
                    Log.d(TAG, "run:  heartbeatBinder null");
                }
                else{
                    Log.d(TAG, "run:  heartbeatBinder not null");
                }
                MotionStateDecider();
                musicPlayerManager.play(currentType);
//                musicBinder.play(currentType);
                mCallBack.updateUi(currentType);

                if(heartrateDetectorState == HeartrateDetectorState.NONE){
                    mCallBack.updateSteps(false,currentSteps);
                }
                else{
                    mCallBack.updateSteps(true,currentHeartRates);
                }
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

    private ServiceConnection heartConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            heartbeatBinder = (HeartbeatService.HeartbeatBinder) service;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public MotionDetectorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        stepIntent = new Intent(this, StepCountService.class);
        musicIntent = new Intent(this, MusicPlayingService.class);

        bindService(stepIntent,stepConnection,BIND_AUTO_CREATE);
        bindService(musicIntent,musicConnection,BIND_AUTO_CREATE);

        startService(stepIntent);
        startService(musicIntent);
        musicPlayerManager = new MusicPlayerManagerExo();

        return mBinder;
    }

    public class MotionBinder extends Binder {
        void play(){
            stepBinder.refreshStep();
//            musicBinder.play(currentType);
            musicPlayerManager.play(currentType);
            if(musicSwitcher==null)
            {
                musicSwitcher = getSwitcher();
                handler.postDelayed(musicSwitcher,timeScale*1000);
            }
        }

        void pause(){
//            musicBinder.pause();
            musicPlayerManager.pause();
            handler.removeCallbacks(musicSwitcher);
            musicSwitcher=null;
        }

        void setType(MusicPlayerManager.MotionState type){
            currentType = type;
            mCallBack.updateUi(type);
        }

        void startHeartDetector(HeartrateDetectorState state){
            heartrateDetectorState = state;
            if(heartbeatBinder == null && state!=HeartrateDetectorState.NONE){
                heartIntent = new Intent(MotionDetectorService.this, HeartbeatService.class);
                bindService(heartIntent,heartConnection,BIND_AUTO_CREATE);

                startService(heartIntent);
            }
        }

        MotionDetectorService getService(){
            return MotionDetectorService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        unbindService(musicConnection);
        unbindService(stepConnection);
        stopService(stepIntent);
        stopService(musicIntent);
        if(heartbeatBinder!=null){
            unbindService(heartConnection);
            stopService(heartIntent);
        }
        handler.removeCallbacks(musicSwitcher);
        musicPlayerManager.release();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    void detectMotionState(){

    }

    public void registerCallback(UpdateUiCallBack mCallBack){
        this.mCallBack = mCallBack;
    }

}
