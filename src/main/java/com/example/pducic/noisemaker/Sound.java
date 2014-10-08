package com.example.pducic.noisemaker;

/**
 * Created by pducic on 08.10.14.
 */
public class Sound {
    private String id;
    private int resourceId;
    private SoundPreview soundPreview;

    public Sound(String id, int resourceId, SoundPreview soundPreview) {
        this.id = id;
        this.resourceId = resourceId;
        this.soundPreview = soundPreview;
    }

    public int getResourceId() {
        return resourceId;
    }

    public SoundPreview getSoundPreview() {
        return soundPreview;
    }
}
