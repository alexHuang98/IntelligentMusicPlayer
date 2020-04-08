package com.intelligentmusicplayer.android.data_access;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.intelligentmusicplayer.android.R;
import com.intelligentmusicplayer.android.musicplaying.MusicData;
import com.intelligentmusicplayer.android.musicplaying.MusicDataProvider;
import com.intelligentmusicplayer.android.musicplaying.MusicPlayerManagerInterface;

import java.util.ArrayList;
import java.util.List;

public class MusicDataAdapter extends BaseExpandableListAdapter {

    MusicDataProvider musicDataProvider;


    public MusicDataAdapter(List<ExpandableMode> groupList){
        musicDataProvider = MusicDataProvider.getInstance();
    }

    @Override
    public int getGroupCount() {//返回第一级List长度

        return musicDataProvider.getState2Source().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {//返回指定groupPosition的第二级List长度
        MusicPlayerManagerInterface.MotionState groupState =
                MusicPlayerManagerInterface.MotionState.values()[groupPosition];

        return musicDataProvider.getState2Source().get(groupState).size();
    }

    @Override
    public Object getGroup(int groupPosition) {//返回一级List里的内容
        MusicPlayerManagerInterface.MotionState groupState =
                MusicPlayerManagerInterface.MotionState.values()[groupPosition];
        return musicDataProvider.getState2Source().get(groupState);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {//返回二级List的内容
        MusicPlayerManagerInterface.MotionState groupState =
                MusicPlayerManagerInterface.MotionState.values()[groupPosition];

        List<MusicData> groupList = musicDataProvider.getState2Source().get(groupState);
        if(childPosition<groupList.size()){
            return groupList.get(childPosition);
        }
        else{
            return groupList.get(groupList.size()-1);
        }

    }

    @Override
    public long getGroupId(int groupPosition) {//返回一级View的id 保证id唯一
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {//返回二级View的id 保证id唯一
        return groupPosition + childPosition;
    }

    /**
     * 指示在对基础数据进行更改时子ID和组ID是否稳定
     * @return
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     *  返回一级父View
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_item, parent,false);
        ((TextView)convertView).setText((String)getGroup(groupPosition));
        return convertView;
    }

    /**
     *  返回二级子View
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_item, parent,false);
        ((TextView)convertView).setText((String)getChild(groupPosition,childPosition));
        return convertView;
    }

    /**
     *  指定位置的子项是否可选
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
