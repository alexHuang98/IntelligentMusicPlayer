package com.intelligentmusicplayer.android.musicplaying;

public abstract class MusicPlayerManagerInterface {
    public enum MotionState{
        SLOW, NORMAL,FAST;

        public static MotionState[] vals = values();

        public MotionState previous() {
            return vals[(this.ordinal() - 1) % vals.length];
        }

        public MotionState next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public abstract void play(MotionState playType);
    public abstract void pause();
    public abstract void release();
}
