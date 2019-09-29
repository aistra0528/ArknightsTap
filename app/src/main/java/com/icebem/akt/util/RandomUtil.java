package com.icebem.akt.util;

public class RandomUtil {
    public static final int RANDOM_P = 5;
    public static final int RANDOM_T = 150;

    private static int random(int i, int r) {
        return i + (int) (Math.random() * r * 2) - r;
    }

    public static int randomP(int p) {
        return random(p, RANDOM_P);
    }

    public static int randomT(int t) {
        return random(t, RANDOM_T);
    }
}