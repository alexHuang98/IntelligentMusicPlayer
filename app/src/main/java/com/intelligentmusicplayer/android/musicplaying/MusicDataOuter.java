package com.intelligentmusicplayer.android.musicplaying;

import android.content.Context;

import android.net.Uri;
import android.os.Environment;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.intelligentmusicplayer.android.MyApplication;
import com.intelligentmusicplayer.android.Utils;

import java.io.File;

public class MusicDataOuter extends MusicData {

    private Uri fileUri;

    public MusicDataOuter(String title,Uri fileUri){
        super(title);
        this.fileUri = fileUri;
    }

    @Override
    public MediaSource getMediaSource() {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(MyApplication.getContext(),
                Util.getUserAgent(MyApplication.getContext(), "intelligentMusicPlayer"));

        MediaSource dataSource =
                new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(fileUri);
        
        return dataSource;
    }
}
