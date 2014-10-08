package com.example.pducic.noisemaker;

import android.content.Context;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pducic on 08.10.14.
 */
public class SoundsConfiguration {

    private Map<Direction, String> soundDirections = new HashMap<Direction, String>();
    private Map<String, Sound> sounds = new HashMap<String, Sound>();
    private Map<String, Integer> soundPoolIds = new HashMap<String, Integer>();


    public SoundsConfiguration(Context context, SoundPool soundPool) {
        //predefined in app
        sounds.put("1", new Sound("1", R.raw.g, new SoundPreview(android.R.color.holo_red_dark)));
        sounds.put("2", new Sound("2", R.raw.a_minor, new SoundPreview(android.R.color.holo_blue_dark)));
        sounds.put("3", new Sound("3", R.raw.c, new SoundPreview(android.R.color.holo_green_dark)));
        sounds.put("4", new Sound("4", R.raw.f, new SoundPreview(android.R.color.black)));

        for (String s : sounds.keySet()) {
            Sound sound = sounds.get(s);
            soundPoolIds.put(s, soundPool.load(context, sound.getResourceId(), 1));
        }

        //configurable
        soundDirections.put(Direction.UP, "1");
        soundDirections.put(Direction.DOWN, "2");
        soundDirections.put(Direction.LEFT, "3");
        soundDirections.put(Direction.RIGHT, "4");

    }

    public int getSoundPoolId(String soundId){
        return soundPoolIds.get(soundId);
    }

    public String getSoundId(Direction direction){
        return soundDirections.get(direction);
    }

    public Sound getSound(String soundId){
        return sounds.get(soundId);
    }
}
