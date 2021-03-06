package com.pducic.jammin.unlock;

import com.pducic.jammin.common.model.Song;
import com.pducic.jammin.common.model.SoundsConfiguration;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by pducic on 26.10.14.
 */
public class LevelContent implements Serializable{

    private static final long serialVersionUID = 1004264492292352655L;

    private String name;
    private Song goal;
    private SoundsConfiguration soundsConfiguration;
    private int mistakeMillis;
    private Collection<String> rewardSounds;

    public LevelContent(String name, Song goal, SoundsConfiguration soundsConfiguration, int mistakeMillis, Collection<String> rewardSounds) {
        this.name = name;
        this.goal = goal;
        this.soundsConfiguration = soundsConfiguration;
        this.mistakeMillis = mistakeMillis;
        this.rewardSounds = rewardSounds;
    }

    public String getName() {
        return name;
    }

    public Song getGoal() {
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
