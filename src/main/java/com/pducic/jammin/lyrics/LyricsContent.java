package com.pducic.jammin.lyrics;

import com.pducic.jammin.common.model.SoundsConfiguration;

import java.io.Serializable;

/**
 * Created by pducic on 16.11.14.
 */
public class LyricsContent implements Serializable {
    private static final long serialVersionUID = -2624701293143124882L;

    private String name;
    private SoundsConfiguration soundsConfiguration;
    private int lyricResourceId;
    /**
     * measure. beats per one takt. 2,4 etc
     */
    private int takt;
    private int bpm;

    public LyricsContent(String name, SoundsConfiguration soundsConfiguration, int lyricResourceId, int takt, int bpm) {
        this.name = name;
        this.soundsConfiguration = soundsConfiguration;
        this.lyricResourceId = lyricResourceId;
        this.takt = takt;
        this.bpm = bpm;
    }

    public String getName() {
        return name;
    }

    public SoundsConfiguration getSoundsConfiguration() {
        return soundsConfiguration;
    }

    public int getLyricResourceId() {
        return lyricResourceId;
    }

    public int getTakt() {
        return takt;
    }

    public int getBpm() {
        return bpm;
    }

    @Override
    public String toString() {
        return name;
    }
}
