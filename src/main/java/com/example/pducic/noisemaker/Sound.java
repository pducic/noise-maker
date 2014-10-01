package com.example.pducic.noisemaker;

/**
 * Created by pducic on 01.10.14..
 */
public class Sound {
    public Sound(int soundId, float amplitude) {
        this.soundId = soundId;
        this.amplitude = amplitude;
    }

    private int soundId;
    private float amplitude;

    public int getSoundId() {
        return soundId;
    }

    public void setSoundId(int soundId) {
        this.soundId = soundId;
    }

    public float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }
}
