package com.intelligentmusicplayer.android.data_access;

import com.intelligentmusicplayer.android.musicplaying.MusicData;

import java.util.List;

public class ExpandableMode {
    private String GroupName;
    private String GroupIcon;
    private List<MusicData> ChildData;

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String GroupName) {
        this.GroupName = GroupName;
    }

    public String getGroupIcon() {
        return GroupIcon;
    }

    public void setGroupIcon(String GroupIcon) {
        this.GroupIcon = GroupIcon;
    }

    public List<MusicData> getChildData() {
        return ChildData;
    }

    public void setChildData(List<MusicData> ChildData) {
        this.ChildData = ChildData;
    }
}
