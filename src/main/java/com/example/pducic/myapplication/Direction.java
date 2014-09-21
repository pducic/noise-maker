package com.example.pducic.myapplication;

/**
 * Created by pducic on 21.09.14..
 */
public enum Direction {

    X(0),
    Y(1),
    Z(2);

    private int index;

    Direction(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static Direction fromValue(int index) {
        for (Direction d : values()) {
            if (d.index == index) return d;
        }
        return null;
    }
}
