package com.example.pducic.noisemaker;

/**
 * Created by pducic on 02.11.14.
 */
public class LyricsHelper {

    public static String cleanupLyricString(String s) {
        return s.replaceAll("\\{.*?\\}", "");
    }

}
