package com.intelligentmusicplayer.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private MusicPlayingService.MusicBinder musicBinder;

    private TextView movingState;

    int currentType;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MusicPlayingService.MusicBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

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
        play_button.setOnClickListener(this);
        pause_button.setOnClickListener(this);

        Intent intent = new Intent(this,MusicPlayingService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);
        currentType = MusicPlayingService.SLOW;
        refreshMovingState(currentType);

    }

    private void refreshMovingState(int state){
        String typeToState[] ={"休闲","较强","激烈"};

        movingState.setText("当前运动状态："+typeToState[state]);
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
            refreshMovingState(currentType);
            musicBinder.play(currentType);
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        if(musicBinder ==null){
            return;
        }
        switch (v.getId()){
            case R.id.play:
                musicBinder.play(currentType);
                break;
            case R.id.pause:
                musicBinder.pause();
                break;
                default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
