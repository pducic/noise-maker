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
    /**
     * interval [0, 1]
     */
    private float tolerance;
    private Collection<String> rewardSounds;

    public Level(String name, Recording goal, SoundsConfiguration soundsConfiguration, float tolerance, Collection<String> rewardSounds) {
        this.name = name;
        this.goal = goal;
        this.soundsConfiguration = soundsConfiguration;
        this.tolerance = tolerance;
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

    public float getTolerance() {
        return tolerance;
    }

    public Collection<String> getRewardSounds() {
        return rewardSounds;
    }

    @Override
    public String toString() {
        return name;
    }
}
