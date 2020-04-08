package com.intelligentmusicplayer.android.musicplaying;

import android.content.Context;
import android.media.MediaPlayer;

import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.util.Util;
import com.intelligentmusicplayer.android.MyApplication;
import com.intelligentmusicplayer.android.R;

public class MusicDataInner extends MusicData {

    private int id;

    public MusicDataInner(String title,int id){
        super(title);
        this.id = id;
    }

    @Override
    public MediaSource getMediaSource() {
        //构建Raw文件播放源--RawResourceDataSource
        DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(id));
        final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(MyApplication.getContext());
        try {
            rawResourceDataSource.open(dataSpec);
        } catch (RawResourceDataSource.RawResourceDataSourceException e) {
            e.printStackTrace();
        }
        //构建ExoPlayer能识别的播放源--MediaSource
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(MyApplication.getContext(),
                Util.getUserAgent(MyApplication.getContext(), "intelligentMusicPlayer"));
        MediaSource dataSource =
                new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(rawResourceDataSource.getUri());

        return dataSource;
    }
}
