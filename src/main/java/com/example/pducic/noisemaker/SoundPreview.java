package com.example.pducic.noisemaker;

import java.io.Serializable;

/**
 * Created by pducic on 08.10.14.
 */
public class SoundPreview implements Serializable{

    private static final long serialVersionUID = -2102626495207411811L;
    private int color;
    private Integer iconResourceId;

    public SoundPreview(int color) {
        this.color = color;
    }

    public SoundPreview(int color, Integer iconResourceId) {
        this.color = color;
        this.iconResourceId = iconResourceId;
    }

    public int getColor() {
        return color;
    }

    public Integer getIconResourceId() {
        return iconResourceId;
    }
}
