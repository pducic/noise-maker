package com.example.pducic.noisemaker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pducic on 08.10.14.
 */
public class Song {
    private long duration;
    private List<Recording> recordings = new ArrayList<Recording>();

    public List<Recording> getRecordings() {
        return recordings;
    }

    public long getDuration() {
        long songLength = 0;
        for (Recording value : recordings) {
            long length = value.getRecordingLength();
            if(length > songLength){
                songLength = length;
            }
        }
        return songLength;
    }
}