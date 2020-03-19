package com.intelligentmusicplayer.android;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import java.util.Random;

public class HeartbeatService extends Service {
    public HeartbeatService() {
    }

    private HeartbeatBinder mBinder = new HeartbeatBinder();

    private int currentHeartRate=50;

    private int timeScale = 5;

    private void generateHeartRate(){
        Random random = new Random();
        int chance = random.nextInt(100);
        int head = random.nextInt(2)*2-1;

        if(chance<20){
            currentHeartRate+=head * 20;
        }
        else if(chance <60){
            currentHeartRate+=head * 10;
        }
        else{
            currentHeartRate+=head * 5;
        }
        if(currentHeartRate>200)
            currentHeartRate=200;
        if(currentHeartRate<50)
            currentHeartRate=50;
    }

    public Handler mHandler = new Handler();

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            generateHeartRate();
            mHandler.postDelayed(this,timeScale*1000);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.postDelayed(mRunnable,timeScale*1000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    public class HeartbeatBinder extends Binder{
        public int getHeartbeat(){
            return currentHeartRate;
        }

    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }
}
