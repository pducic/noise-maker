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
                soundPreviews.put(sound.getKey(), getSoundPreview(sound.getSoundGesture()));
            } else {
                soundPreviews.put(sound.getKey(), sound.getDefaultSoundPreview());
            }
        }
    }

    private SoundPreview getSoundPreview(SoundGesture soundGesture){
        return new SoundPreview(getColor(soundGesture.getConfigurationButtonId()), getIconId(soundGesture.getDirection()));
    }

    private Integer getIconId(Direction direction) {
        switch (direction){
            case RIGHT: return R.drawable.icon_right;
            case LEFT: return R.drawable.icon_left;
            case UP: return R.drawable.icon_down;
            case DOWN: return R.drawable.icon_up;
        }
        return null;
    }

    private int getColor(SoundGesture.ConfigurationButtonId soundConfigurationButtonId){
        switch (soundConfigurationButtonId){
            case LEFT: return R.color.left_button;
            case RIGHT: return R.color.right_button;
        }
        return android.R.color.black;
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
