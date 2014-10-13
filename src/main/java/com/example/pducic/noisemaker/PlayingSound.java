package com.example.pducic.noisemaker;

/**
 * Created by pducic on 01.10.14.
 *
 */
public class PlayingSound {
    public PlayingSound(String soundId, float amplitude, long time) {
        this.time = time;
        this.soundId = soundId;
        this.amplitude = amplitude;
    }

    public PlayingSound(String soundId, float amplitude) {
        this(soundId, Math.abs(amplitude), 0);
    }

    private long time;
    private String soundId;
    private float amplitude;

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public String getSoundId() {
        return soundId;
    }

    public float getAmplitude() {
        return amplitude;
    }

    @Override
    public String toString() {
        return "PlayingSound{" +
                "time=" + time +
                ", soundId='" + soundId + '\'' +
                ", amplitude=" + amplitude +
                '}';
    }
}
