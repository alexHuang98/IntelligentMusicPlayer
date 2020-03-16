package com.intelligentmusicplayer.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.intelligentmusicplayer.android.musicplaying.MusicPlayingService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private static final String TAG = "MainActivityHPY";
    private MotionDetectorService.MotionBinder motionBinder;

    private TextView movingState, currentSteps;

    private MotionDetectorService motionDetectorService;

    int currentType;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 1:
                    refreshMovingState(msg.arg1);
                    if(msg.arg2!=-1){
                        refreshCurrentSteps(msg.arg2);
                    }
                    break;
                    default:
            }
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            motionBinder = (MotionDetectorService.MotionBinder) service;
            if(motionBinder==null){
                Log.d(TAG, "onServiceConnected: failed");
            }
            else{
                Log.d(TAG, "onServiceConnected: successed");
            }
            motionDetectorService = motionBinder.getService();
            motionDetectorService.registerCallback(new UpdateUiCallBack() {
                @Override
                public void updateUi(int type, int steps) {
                    Message msg = new Message();
                    msg.what = 1;
                    msg.arg1 = type;
                    msg.arg2 = steps;
                    mHandler.sendMessage(msg);
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceConnected: failed");
        }
    };

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button play_button = (Button) findViewById(R.id.play);
        Button pause_button = (Button) findViewById(R.id.pause);
        movingState = (TextView) findViewById(R.id.moving_state);
        currentSteps =(TextView) findViewById(R.id.current_steps);
        play_button.setOnClickListener(this);
        pause_button.setOnClickListener(this);

        Intent intent = new Intent(this,MotionDetectorService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);
        currentType = MusicPlayingService.SLOW;
        refreshMovingState(currentType);
    }

    private void refreshMovingState(int state){
        String typeToState[] ={"休闲","较强","激烈"};

        movingState.setText("当前运动状态："+typeToState[state]);
    }

    private void refreshCurrentSteps(Integer steps){


        currentSteps.setText("当前步数："+steps.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.simulation:
                if (!item.isChecked()) {
                    item.setChecked(true);

                    Snackbar.make(toolbar, "开始模拟", Snackbar.LENGTH_SHORT).setAction("Undo",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(MainActivity.this, "停止模拟", Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                }
                else{
                    item.setChecked(false);
                    Toast.makeText(MainActivity.this, "停止模拟", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.switch_slow:
                currentType=MusicPlayingService.SLOW;
                break;
            case R.id.switch_normal:
                currentType=MusicPlayingService.NORMAL;
                break;
            case R.id.switch_fast:
                currentType=MusicPlayingService.FAST;
                break;
                default:

        }
        if(item.getItemId()==R.id.switch_slow || item.getItemId()==R.id.switch_normal
                ||item.getItemId()==R.id.switch_fast){
            //refreshMovingState(currentType);
            motionBinder.setType(currentType);
            motionBinder.play();
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        if(motionBinder ==null){
            Toast.makeText(this,"BindFail",Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()){
            case R.id.play:

                motionBinder.play();
                break;
            case R.id.pause:
                motionBinder.pause();
                break;
                default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }



    private Uri rawToUri(int id){
        String uriStr = "android.resource://" + getPackageName() + "/" + id;
        return Uri.parse(uriStr);
    }
}
