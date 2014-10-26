package com.example.pducic.noisemaker;

import java.io.Serializable;
import java.util.List;

/**
 * Created by pducic on 02.10.14
 */
public class Recording implements Serializable{
    private static final long serialVersionUID = -1775803374902747440L;

    private String name;
    private List<PlayingSound> playingSounds;

    public Recording(String name, List<PlayingSound> playingSounds) {
        this.name = name;
        this.playingSounds = playingSounds;
    }

    public String getName() {
        return name;
    }

    public List<PlayingSound> getPlayingSounds() {
        return playingSounds;
    }

    public long getRecordingLength(){
        List<PlayingSound> playingSounds = getPlayingSounds();
        if(playingSounds.isEmpty()){
            return 0;
        }
        return playingSounds.get(playingSounds.size()-1).getTime();
    }
}
