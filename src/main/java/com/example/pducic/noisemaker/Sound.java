package com.example.pducic.noisemaker;

/**
 * Created by pducic on 01.10.14..
 */
public class Sound {
    public Sound(long time, int soundId, float amplitude) {
        this.time = time;
        this.soundId = soundId;
        this.amplitude = amplitude;
    }

    public Sound(int soundId, float amplitude) {
        this(0, soundId, amplitude);
    }

    private long time;
    private int soundId;
    private float amplitude;

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public int getSoundId() {
        return soundId;
    }

    public float getAmplitude() {
        return amplitude;
    }

}
