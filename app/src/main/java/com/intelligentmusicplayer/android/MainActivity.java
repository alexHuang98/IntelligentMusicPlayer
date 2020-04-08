package com.intelligentmusicplayer.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.intelligentmusicplayer.android.musicplaying.MusicPlayerManager;
import com.intelligentmusicplayer.android.musicplaying.MusicPlayingService;

public class MainActivity extends BaseActivity implements View.OnClickListener{


    private static final String TAG = "MainActivityHPY";
    private MotionDetectorService.MotionBinder motionBinder;

    private TextView movingState, currentSteps;

    private MotionDetectorService motionDetectorService;

    private DrawerLayout mDrawerLayout;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 1:
                    refreshMovingState((MusicPlayerManager.MotionState)msg.obj);
                    break;
                case 2:
                    refreshCurrentSteps(msg.arg2,msg.arg1);
                    break;
                    default:
            }
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            motionBinder = (MotionDetectorService.MotionBinder) service;
            motionDetectorService = motionBinder.getService();
            motionDetectorService.registerCallback(new UpdateUiCallBack() {
                @Override
                public void updateUi(MusicPlayerManager.MotionState type) {
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = type;
                    mHandler.sendMessage(msg);
                }

                @Override
                public void updateSteps(boolean isHeartRated, int steps){
                    Message msg = new Message();
                    msg.what = 2;
                    msg.arg1 = steps;
                    if(isHeartRated)
                    {
                        msg.arg2 =1;

                    }
                    else
                        msg.arg2=0;
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
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        }

        Button play_button = (Button) findViewById(R.id.play);
        Button pause_button = (Button) findViewById(R.id.pause);
        Button file_button = (Button) findViewById(R.id.file_choose);
        NavigationView navView = (NavigationView)findViewById(R.id.nav_view);
        movingState = (TextView) findViewById(R.id.moving_state);
        currentSteps =(TextView) findViewById(R.id.current_steps);

        play_button.setOnClickListener(this);
        pause_button.setOnClickListener(this);
        file_button.setOnClickListener(this);
        navView.setCheckedItem(R.id.nav_main);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    default:
                }
                return false;
            }
        });

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.
                WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.
                    WRITE_EXTERNAL_STORAGE},1);
        }
        else{
            Utils.isGranted=true;
        }

        Intent intent = new Intent(getApplicationContext(),MotionDetectorService.class);

        bindService(intent,connection,BIND_AUTO_CREATE);
        refreshMovingState(MusicPlayerManager.MotionState.SLOW);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Utils.isGranted=true;
                }
                else{
                    Toast.makeText(this,"拒绝权限只能播放系统自带歌曲",Toast.LENGTH_SHORT).show();
                    Utils.isGranted = false;
                }
        }
    }

    private void refreshMovingState(MusicPlayerManager.MotionState state){

        movingState.setText("当前运动状态："+Utils.state2str.get(state));
    }

    private void refreshCurrentSteps(int detectorType, Integer steps){
        String typeToDetector[] = {"步数","心率"};
        currentSteps.setText("当前"+typeToDetector[detectorType]+"："+steps.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.simulation:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    motionBinder.startHeartDetector(MotionDetectorService.HeartrateDetectorState.SIMULATION);

                    Snackbar.make(toolbar, "开始模拟", Snackbar.LENGTH_SHORT).setAction("Undo",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    motionBinder.startHeartDetector(MotionDetectorService.HeartrateDetectorState.NONE);
                                    Toast.makeText(MainActivity.this, "停止模拟", Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                }
                else{
                    item.setChecked(false);
                    motionBinder.startHeartDetector(MotionDetectorService.HeartrateDetectorState.NONE);
                    Toast.makeText(MainActivity.this, "停止模拟", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.switch_slow:
            case R.id.switch_normal:
            case R.id.switch_fast:
                MusicPlayerManager.MotionState selectedState = Utils.selection2id.get(item.getItemId());
                motionBinder.setType(selectedState);
                motionBinder.play();
                refreshMovingState(selectedState);
                break;
                default:
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
            case R.id.file_choose:
                pickFile(v);
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

    public void pickFile(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        this.startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            // 用户未选择任何文件，直接返回
            return;
        }

        Uri uri = data.getData(); // 获取用户选择文件的URI
        String path = Utils.getPath(this,uri);

        Log.d(TAG, "onActivityResult: "+path);
    }
}
