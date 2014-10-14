package com.example.pducic.noisemaker;

/**
 * Created by pducic on 08.10.14
 */
public class Sound {
    @SuppressWarnings("unused")
    private String name;
    private int resourceId;
    private SoundPreview soundPreview;

    public Sound(String name, int resourceId, SoundPreview soundPreview) {
        this.name = name;
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
