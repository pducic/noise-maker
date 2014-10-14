package com.example.pducic.noisemaker;

import android.content.Context;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pducic on 08.10.14
 */
public class SoundsConfiguration {

    private Map<SoundConfiguration, String> soundDirections = new HashMap<SoundConfiguration, String>();
    private Map<String, Sound> sounds = new HashMap<String, Sound>();
    private Map<String, Integer> soundPoolIds = new HashMap<String, Integer>();


    public SoundsConfiguration(Context context, SoundPool soundPool) {
        //predefined in app / storage
        sounds.put("g_G", new Sound("guitar G", R.raw.g, new SoundPreview(android.R.color.holo_red_dark)));
        sounds.put("g_a", new Sound("guitar a", R.raw.a_minor, new SoundPreview(android.R.color.holo_blue_dark)));
        sounds.put("g_C", new Sound("guitar C", R.raw.c, new SoundPreview(android.R.color.holo_green_dark)));
        sounds.put("g_F", new Sound("guitar F", R.raw.f, new SoundPreview(android.R.color.black)));
        sounds.put("d_hat", new Sound("drum hat", R.raw.drum_hat_open, new SoundPreview(android.R.color.holo_orange_dark)));
        sounds.put("d_kick", new Sound("drum kick", R.raw.drum_prac_kick, new SoundPreview(android.R.color.holo_purple)));
        sounds.put("d_snare", new Sound("drum snare", R.raw.drum_prac_snare, new SoundPreview(android.R.color.darker_gray)));
        sounds.put("d_snare_rim", new Sound("drum snare rim", R.raw.drum_prac_snare_rim, new SoundPreview(android.R.color.holo_blue_bright)));

        for (String s : sounds.keySet()) {
            Sound sound = sounds.get(s);
            soundPoolIds.put(s, soundPool.load(context, sound.getResourceId(), 1));
        }

        //configurable
        soundDirections.put(new SoundConfiguration(Direction.UP, ConfigurationButtonId.LEFT), "g_G");
        soundDirections.put(new SoundConfiguration(Direction.DOWN, ConfigurationButtonId.LEFT), "g_F");
        soundDirections.put(new SoundConfiguration(Direction.LEFT, ConfigurationButtonId.LEFT), "g_C");
        soundDirections.put(new SoundConfiguration(Direction.RIGHT, ConfigurationButtonId.LEFT), "g_a");
        soundDirections.put(new SoundConfiguration(Direction.UP, ConfigurationButtonId.RIGHT), "d_hat");
        soundDirections.put(new SoundConfiguration(Direction.DOWN, ConfigurationButtonId.RIGHT), "d_snare");
        soundDirections.put(new SoundConfiguration(Direction.LEFT, ConfigurationButtonId.RIGHT), "d_kick");
        soundDirections.put(new SoundConfiguration(Direction.RIGHT, ConfigurationButtonId.RIGHT), "d_snare_rim");
    }

    public int getSoundPoolId(String soundId){
        return soundPoolIds.get(soundId);
    }

    public String getSoundId(Direction direction, ConfigurationButtonId buttonId){
        return soundDirections.get(new SoundConfiguration(direction, buttonId));
    }

    public Sound getSound(String soundId){
        return sounds.get(soundId);
    }

    public enum ConfigurationButtonId {
        LEFT,
        RIGHT
    }

    class SoundConfiguration{
        Direction direction;
        ConfigurationButtonId configurationButtonId;

        SoundConfiguration(Direction direction, ConfigurationButtonId configurationButtonId) {
            this.direction = direction;
            this.configurationButtonId = configurationButtonId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SoundConfiguration that = (SoundConfiguration) o;

            return configurationButtonId == that.configurationButtonId && direction == that.direction;

        }

        @Override
        public int hashCode() {
            int result = direction.hashCode();
            result = 31 * result + configurationButtonId.hashCode();
            return result;
        }
    }
}
