package com.intelligentmusicplayer.android;

import com.intelligentmusicplayer.android.musicplaying.MusicPlayerManager;

/**
 * 步数更新回调
 * Created by dylan on 16/9/27.
 */
public interface UpdateUiCallBack {
    /**
     * 更新UI步数
     *
     *
     *
     * @param type 当前状态
     */
    void updateUi(MusicPlayerManager.MotionState type);
    void updateSteps(boolean isHeartRated, int steps);
}
