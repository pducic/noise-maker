package com.example.pducic.noisemaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by pducic on 26.10.14.
 */
public class MainConfiguration {

    // SoundPool constants
    public static final int MAX_STREAMS = 3; // 3 sounds for now
    public static final int SRC_QUALITY = 0; // Android Docs: "The sample-rate converter quality. Currently has no effect. Use 0 for the default"
    public static final int IGNORE_EVENTS_AFTER_SOUND = 300;
    public static final float POSITIVE_COUNTER_THRESHOLD = (float) 5.0;
    public static final long DEFAULT_TEMPO = 1000;
    public static final int MAX_RECORDING_SIZE = 1000;
    /**
     * [1-100] greater -> rougher
     */
    public static final int SEEKBAR_GRANULARITY = 1;

    public static SoundsConfiguration getDefaultSoundConfiguration() {
        LinkedList<Sound> sounds = new LinkedList<Sound>();
        //predefined in app / storage
        sounds.add(new Sound("stand_by_me_a_major", "guitar G", R.raw.stand_by_me_a_major, new SoundPreview(android.R.color.holo_red_dark), new SoundGesture(Direction.UP, SoundGesture.ConfigurationButtonId.LEFT)));
        sounds.add(new Sound("stand_by_me_cis_minor", "guitar a", R.raw.stand_by_me_cis_minor, new SoundPreview(android.R.color.holo_blue_dark), new SoundGesture(Direction.RIGHT, SoundGesture.ConfigurationButtonId.LEFT)));
        sounds.add(new Sound("stand_by_me_e_major", "guitar C", R.raw.stand_by_me_e_major, new SoundPreview(android.R.color.holo_green_dark), new SoundGesture(Direction.LEFT, SoundGesture.ConfigurationButtonId.LEFT)));
        sounds.add(new Sound("stand_by_me_h_major", "guitar F", R.raw.stand_by_me_h_major, new SoundPreview(android.R.color.black), new SoundGesture(Direction.DOWN, SoundGesture.ConfigurationButtonId.LEFT)));
        sounds.add(new Sound("drum_hat_open", "drum hat", R.raw.drum_hat_open, new SoundPreview(android.R.color.holo_orange_dark), new SoundGesture(Direction.UP, SoundGesture.ConfigurationButtonId.RIGHT)));
        sounds.add(new Sound("drum_prac_kick", "drum kick", R.raw.drum_prac_kick, new SoundPreview(android.R.color.holo_purple), new SoundGesture(Direction.LEFT, SoundGesture.ConfigurationButtonId.RIGHT)));
        sounds.add(new Sound("drum_prac_snare", "drum snare", R.raw.drum_prac_snare, new SoundPreview(android.R.color.darker_gray), new SoundGesture(Direction.DOWN, SoundGesture.ConfigurationButtonId.RIGHT)));
        sounds.add(new Sound("drum_prac_snare_rim", "drum snare rim", R.raw.drum_prac_snare_rim, new SoundPreview(android.R.color.holo_blue_bright), new SoundGesture(Direction.RIGHT, SoundGesture.ConfigurationButtonId.RIGHT)));

        return new SoundsConfiguration(sounds, false);
    }

    public static List<Level> getLevels() {
        return new ArrayList<Level>() {{
            add(new Level("Level 1", new Recording("Level 1", new ArrayList<PlayingSound>() {{
                add(new PlayingSound("drum_prac_snare_rim", 0f, 2000));
                add(new PlayingSound("drum_prac_kick", 0f, 3000));
            }}), new SoundsConfiguration(new ArrayList<Sound>() {{
                add(new Sound("drum_hat_open", "drum hat", R.raw.drum_hat_open, new SoundPreview(android.R.color.holo_orange_dark), new SoundGesture(Direction.UP, SoundGesture.ConfigurationButtonId.RIGHT)));
                add(new Sound("drum_prac_kick", "drum kick", R.raw.drum_prac_kick, new SoundPreview(android.R.color.holo_purple), new SoundGesture(Direction.LEFT, SoundGesture.ConfigurationButtonId.RIGHT)));
                add(new Sound("drum_prac_snare", "drum snare", R.raw.drum_prac_snare, new SoundPreview(android.R.color.darker_gray), new SoundGesture(Direction.DOWN, SoundGesture.ConfigurationButtonId.RIGHT)));
                add(new Sound("drum_prac_snare_rim", "drum snare rim", R.raw.drum_prac_snare_rim, new SoundPreview(android.R.color.holo_blue_bright), new SoundGesture(Direction.RIGHT, SoundGesture.ConfigurationButtonId.RIGHT)));
            }}, true), 150, Collections.<String>emptyList()));

            add(new Level("Level 2", new Recording("Level 2", new ArrayList<PlayingSound>() {{
                add(new PlayingSound("drum_prac_kick", 0f, 1000));
                add(new PlayingSound("drum_prac_snare_rim", 0f, 2000));
                add(new PlayingSound("drum_prac_snare_rim", 0f, 1500));
                add(new PlayingSound("drum_prac_kick", 0f, 3000));
                add(new PlayingSound("drum_hat_open", 0f, 4000));
            }}), new SoundsConfiguration(new ArrayList<Sound>() {{
                add(new Sound("drum_hat_open", "drum hat", R.raw.drum_hat_open, new SoundPreview(android.R.color.holo_orange_dark), new SoundGesture(Direction.UP, SoundGesture.ConfigurationButtonId.RIGHT)));
                add(new Sound("drum_prac_kick", "drum kick", R.raw.drum_prac_kick, new SoundPreview(android.R.color.holo_purple), new SoundGesture(Direction.LEFT, SoundGesture.ConfigurationButtonId.RIGHT)));
                add(new Sound("drum_prac_snare", "drum snare", R.raw.drum_prac_snare, new SoundPreview(android.R.color.darker_gray), new SoundGesture(Direction.DOWN, SoundGesture.ConfigurationButtonId.LEFT)));
                add(new Sound("drum_prac_snare_rim", "drum snare rim", R.raw.drum_prac_snare_rim, new SoundPreview(android.R.color.holo_blue_bright), new SoundGesture(Direction.RIGHT, SoundGesture.ConfigurationButtonId.LEFT)));
            }}, true), 150, Collections.<String>emptyList()));
        }};
    }

}
