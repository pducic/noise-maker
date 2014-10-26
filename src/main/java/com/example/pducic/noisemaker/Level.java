package com.example.pducic.noisemaker;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by pducic on 26.10.14.
 */
public class Level implements Serializable{

    private static final long serialVersionUID = 1004264492292352655L;

    private String name;
    private Recording goal;
    private SoundsConfiguration soundsConfiguration;
    private int mistakeMillis;
    private Collection<String> rewardSounds;

    public Level(String name, Recording goal, SoundsConfiguration soundsConfiguration, int mistakeMillis, Collection<String> rewardSounds) {
        this.name = name;
        this.goal = goal;
        this.soundsConfiguration = soundsConfiguration;
        this.mistakeMillis = mistakeMillis;
        this.rewardSounds = rewardSounds;
    }

    public String getName() {
        return name;
    }

    public Recording getGoal() {
        return goal;
    }

    public SoundsConfiguration getSoundsConfiguration() {
        return soundsConfiguration;
    }

    public int getMistakeMillis() {
        return mistakeMillis;
    }

    public Collection<String> getRewardSounds() {
        return rewardSounds;
    }

    @Override
    public String toString() {
        return name;
    }
}
