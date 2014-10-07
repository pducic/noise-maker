package com.example.pducic.noisemaker;

import java.util.List;

/**
 * Created by pducic on 02.10.14
 */
public class Recording {
    private String name;
    private List<Sound> sounds;

    public Recording(String name, List<Sound> sounds) {
        this.name = name;
        this.sounds = sounds;
    }

    public String getName() {
        return name;
    }

    public List<Sound> getSounds() {
        return sounds;
    }

    public long getRecordingLength(){
        List<Sound> sounds = getSounds();
        if(sounds.isEmpty()){
            return 0;
        }
        return sounds.get(sounds.size()-1).getTime();
    }
}
