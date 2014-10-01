package com.example.pducic.noisemaker;

/**
 * Created by pducic on 21.09.14..
 */
public enum Direction {

    UP(0),
    DOWN(1),
    LEFT(2),
    RIGHT(3);

    private int index;

    Direction(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

}
