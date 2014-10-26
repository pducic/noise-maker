package com.example.pducic.noisemaker;

import android.content.Context;
import android.media.SoundPool;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pducic on 08.10.14
 */
public class SoundsConfiguration implements Serializable{

    private static final long serialVersionUID = -1954680715157594242L;
    private Map<SoundGesture, String> soundDirections;
    private Map<String, Integer> soundPoolIds;
    private Map<String, SoundPreview> soundPreviews;
    private Collection<Sound> sounds;

    public SoundsConfiguration(Collection<Sound> soundsCollection, boolean previewDirections) {
        soundDirections = new HashMap<SoundGesture, String>();
        soundPreviews = new HashMap<String, SoundPreview>();
        sounds = soundsCollection;
        for (Sound sound : soundsCollection) {
            soundDirections.put(sound.getSoundGesture(), sound.getKey());
            if (previewDirections) {
                //TODO add logic for previewDirections: if set true load icon by direction + color by config key; default otherwise
                soundPreviews.put(sound.getKey(), sound.getDefaultSoundPreview());
            } else {
                soundPreviews.put(sound.getKey(), sound.getDefaultSoundPreview());
            }
        }
    }

    public void init(Context context, SoundPool soundPool){
        soundPoolIds = new HashMap<String, Integer>();
        for (Sound sound : sounds) {
            soundPoolIds.put(sound.getKey(), soundPool.load(context, sound.getResourceId(), 1));
        }
    }

    public Integer getSoundPoolId(String soundId) {
        if(soundPoolIds == null){
            throw new IllegalStateException("Init method should be called before first use");
        }
        Integer result = soundPoolIds.get(soundId);
        if(result == null){
            throw new IllegalStateException("SoundPool failed to instantiate correctly");
        }
        return result;
    }

    public String getSoundId(Direction direction, SoundGesture.ConfigurationButtonId buttonId) {
        return soundDirections.get(new SoundGesture(direction, buttonId));
    }

    public SoundPreview getSoundPreview(String soundId) {
        return soundPreviews.get(soundId);
    }
}
