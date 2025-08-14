package com.iridian.movie.social.util;

import static java.lang.Math.floor;
import static java.lang.Math.random;

public class PrimaryKey {

    private final static String[] ENCODING = {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "A", "B", "C", "D", "E", "F", "G", "H", "J", "K",
        "M", "N", "P", "Q", "R", "S", "T", "V", "W", "X",
        "Y", "Z"};

    private final static long ENCODING_LENGTH = 32;

    public static String get() {
        return encodeTime(getTime(), 10) + encodeRandom(22);
    }

    protected static String encodeTime(long time, long length) {
        String out = "";
        while (length > 0) {
            int mod = (int) (time % ENCODING_LENGTH);
            if (length == 8) {
                out = '-' + out;
            }
            out = ENCODING[mod] + out;
            time = (time - mod) / ENCODING_LENGTH;
            length--;
        }
        return out;
    }

    protected static String encodeRandom(long length) {
        String out = "";
        while (length > 0) {
            int rand = (int) (floor(ENCODING_LENGTH * getRand()));
            if (length == 2 || length == 6 || length == 10) {
                out = '-' + out;
            }
            out = ENCODING[rand] + out;
            length--;
        }
        return out;
    }

    protected static double getRand() {
        return random();
    }

    protected static long getTime() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        System.out.println(get());
    }
}
