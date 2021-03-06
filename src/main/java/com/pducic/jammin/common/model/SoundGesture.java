package com.pducic.jammin.common.model;

import java.io.Serializable;

/**
 * Created by pducic on 26.10.14.
 */
public class SoundGesture implements Serializable{

    private static final long serialVersionUID = -3618347970946136489L;

    public enum ConfigurationButtonId {
        LEFT,
        RIGHT
    }

    private Direction direction;
    private ConfigurationButtonId configurationButtonId;

    public SoundGesture(Direction direction, ConfigurationButtonId configurationButtonId) {
        this.direction = direction;
        this.configurationButtonId = configurationButtonId;
    }

    public ConfigurationButtonId getConfigurationButtonId() {
        return configurationButtonId;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ((Object) this).getClass() != o.getClass()) return false;

        SoundGesture that = (SoundGesture) o;

        if (configurationButtonId != that.configurationButtonId) return false;
        if (direction != that.direction) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = direction.hashCode();
        result = 31 * result + configurationButtonId.hashCode();
        return result;
    }
}
