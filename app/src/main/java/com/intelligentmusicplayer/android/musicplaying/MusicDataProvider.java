package com.intelligentmusicplayer.android.musicplaying;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaDataSource;
import android.net.Uri;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.intelligentmusicplayer.android.MainActivity;
import com.intelligentmusicplayer.android.MyApplication;
import com.intelligentmusicplayer.android.R;
import com.intelligentmusicplayer.android.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicDataProvider {

    private Map<MusicPlayerManager.MotionState, List<MusicData>> state2source;


    private static MusicDataProvider instance;

    public static MusicDataProvider getInstance(){
        if(instance ==null){
            instance = new MusicDataProvider();
        }
        return instance;
    }



    public MusicDataProvider(){
        state2source = new HashMap<>();

        for(MusicPlayerManagerInterface.MotionState state : MusicPlayerManagerInterface.MotionState.values()){
            state2source.put(state,new ArrayList<MusicData>());
        }
//        state2source.get(MusicPlayerManager.MotionState.SLOW).add(new MusicDataInner("October",R.raw.slow));
        state2source.get(MusicPlayerManager.MotionState.NORMAL).add(new MusicDataInner("Happy",R.raw.normal));
        state2source.get(MusicPlayerManager.MotionState.FAST).add(new MusicDataInner("I Can't Win",R.raw.fast));

        loadData();
    }

    public void addData(MusicPlayerManagerInterface.MotionState state,MusicData data){
        if(data!=null&&data.getMediaSource()!=null){
            state2source.get(state).add(data);
        }
    }

    public void loadData(){
        MusicData data;
        if(Utils.isGranted){
            data = new MusicDataOuter("test",
                Uri.fromFile(new File(
                        "/storage/emulated/0/MIUI/music/小米预置无损音乐/小米预置：绅士_薛之谦.wav"))
//                    Uri.parse("content://com.android.fileexplorer.myprovider/external_files/MIUI/music/%E5%B0%8F%E7%B1%B3%E9%A2%84%E7%BD%AE%E6%97%A0%E6%8D%9F%E9%9F%B3%E4%B9%90/%E5%B0%8F%E7%B1%B3%E9%A2%84%E7%BD%AE%EF%BC%9A%E7%BB%85%E5%A3%AB_%E8%96%9B%E4%B9%8B%E8%B0%A6.wav")
            );

        }
        else{
            data = new MusicDataInner("October",R.raw.slow);
        }
        addData(MusicPlayerManagerInterface.MotionState.SLOW,data);
    }

    public Map<MusicPlayerManager.MotionState, List<MusicData>> getState2Source(){
        return state2source;
    }

    public MediaSource getMediaSource(MusicPlayerManagerInterface.MotionState state){
        List<MusicData> dataList = state2source.get(state);
        List<MediaSource> sourceList = new ArrayList<>();

        for(MusicData musicData: dataList) {
            MediaSource current = musicData.getMediaSource();
            if(current!=null)
                sourceList.add(current);
        }

        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource();
        concatenatedSource.addMediaSources(sourceList);

        return concatenatedSource;
    }

}
