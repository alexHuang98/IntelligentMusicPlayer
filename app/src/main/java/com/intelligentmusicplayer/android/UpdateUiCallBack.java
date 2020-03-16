package com.intelligentmusicplayer.android;

/**
 * 步数更新回调
 * Created by dylan on 16/9/27.
 */
public interface UpdateUiCallBack {
    /**
     * 更新UI步数
     *
     * @param currentSteps 步数
     *
     * @param type 当前状态
     */
    void updateUi(int type, int currentSteps);
}
