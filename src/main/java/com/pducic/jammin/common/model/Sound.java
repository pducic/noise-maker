package com.pducic.jammin.common.model;

import java.io.Serializable;

/**
 * Created by pducic on 08.10.14
 */
public class Sound implements Serializable{
    private static final long serialVersionUID = 6378802080572447400L;

    private String key;
    private String name;
    private int resourceId;
    private SoundPreview defaultSoundPreview;
    private SoundGesture soundGesture;

    public Sound(String key, String name, int resourceId, SoundPreview defaultSoundPreview, SoundGesture soundGesture) {
        this.key = key;
        this.name = name;
        this.resourceId = resourceId;
        this.defaultSoundPreview = defaultSoundPreview;
        this.soundGesture = soundGesture;
    }

    public int getResourceId() {
        return resourceId;
    }

    public SoundPreview getDefaultSoundPreview() {
        return defaultSoundPreview;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public SoundGesture getSoundGesture() {
        return soundGesture;
    }
}
