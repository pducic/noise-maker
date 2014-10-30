package com.example.pducic.noisemaker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pducic on 08.10.14.
 */
public class Song implements Serializable{
    private static final long serialVersionUID = 4743858136489182522L;
    private List<Recording> recordings;

    public Song() {
        recordings = new ArrayList<Recording>();
    }

    public Song(List<Recording> recordings) {
        this.recordings = recordings;
    }

    public List<Recording> getRecordings() {
        return recordings;
    }

    public long getDuration() {
        long songLength = 0;
        for (Recording value : recordings) {
            long length = value.getDuration();
            if(length > songLength){
                songLength = length;
            }
        }
        return songLength;
    }
}