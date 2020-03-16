package com.intelligentmusicplayer.android.musicplaying;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.intelligentmusicplayer.android.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends MediaBrowserServiceCompat {
    private MediaSessionCompat mSession;

    private PlaybackStateCompat mPlaybackState;

    private MediaPlayer mMediaPlayer;

    public static final String MEDIA_ID_ROOT = "player";

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.detach();


        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, ""+ R.raw.slow)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "xxx")
                .build();
        ArrayList<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        //遍历
        mediaItems.add(createMediaItem(metadata));

        //向Browser发送数据
        result.sendResult(mediaItems);
    }

    private MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata){
        return new MediaBrowserCompat.MediaItem(
                metadata.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        );
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(MEDIA_ID_ROOT,null);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlaybackState = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE,0,1.0f)
                .build();

        mSession = new MediaSessionCompat(this,"MusicService");
        mSession.setCallback(SessionCallback);//设置回调
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setPlaybackState(mPlaybackState);

        //设置token后会触发MediaBrowserCompat.ConnectionCallback的回调方法
        //表示MediaBrowser与MediaBrowserService连接成功
        setSessionToken(mSession.getSessionToken());

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(PreparedListener);
        mMediaPlayer.setOnCompletionListener(CompletionListener);
    }

    private MediaSessionCompat.Callback SessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            if(mPlaybackState.getState() == PlaybackStateCompat.STATE_PAUSED){
                mMediaPlayer.start();
                mPlaybackState = new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING,0,1.0f)
                        .build();
                mSession.setPlaybackState(mPlaybackState);
            }
        }

        @Override
        public void onPause() {
            if(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING){
                mMediaPlayer.pause();
                mPlaybackState = new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PAUSED,0,1.0f)
                        .build();
                mSession.setPlaybackState(mPlaybackState);
            }
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            try {
                switch (mPlaybackState.getState()){
                    case PlaybackStateCompat.STATE_PLAYING:
                    case PlaybackStateCompat.STATE_PAUSED:
                    case PlaybackStateCompat.STATE_NONE:
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(MusicService.this,uri);
                        mMediaPlayer.prepare();//准备同步
                        mPlaybackState = new PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_CONNECTING,0,1.0f)
                                .build();
                        mSession.setPlaybackState(mPlaybackState);
                        //我们可以保存当前播放音乐的信息，以便客户端刷新UI
                        mSession.setMetadata(new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,extras.getString("title"))
                                .build()
                        );
                        break;
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    };

    /**
     * 监听MediaPlayer.prepare()
     */
    private MediaPlayer.OnPreparedListener PreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mMediaPlayer.start();
            mPlaybackState = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING,0,1.0f)
                    .build();
            mSession.setPlaybackState(mPlaybackState);
        }
    } ;

    /**
     * 监听播放结束的事件
     */
    private MediaPlayer.OnCompletionListener CompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mPlaybackState = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_NONE,0,1.0f)
                    .build();
            mSession.setPlaybackState(mPlaybackState);
            mMediaPlayer.reset();

        }
    };


}