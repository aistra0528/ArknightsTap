package com.icebem.akt.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ResolutionConfig {
    public static final float RATIO_MAX = 2.5f;
    public static final float RATIO_MIN = 1.25f;
    public static final float RATIO_DEFAULT = 1.77f;
    public static final int[][] RESOLUTION_CONFIG = {
            {2340, 1080, 2125, 980, 1835, 700},
            // TODO
            {1280, 720, 1100, 670, 1100, 650},
            {1440, 720, 1330, 670, 1200, 650},
            {1560, 720, 1470, 670, 1230, 650},
            {1920, 1080, 1650, 1000, 1650, 960},
            {2160, 1080, 2000, 1000, 1800, 960},
            {2232, 1080, 2030, 1000, 1810, 960},
            {2244, 1080, 2050, 1000, 1830, 960},
            {2248, 1080, 2050, 1000, 1830, 960},
            {2280, 1080, 2090, 1000, 1840, 960},
            {2310, 1080, 2110, 1000, 1850, 960},
            {2560, 1096, 2350, 1030, 1990, 970},
            {1920, 1200, 1730, 1110, 1660, 930},
            {2560, 1440, 2200, 1330, 2200, 1280},
            {2880, 1440, 2660, 1330, 2400, 1280},
            {2960, 1440, 2800, 1330, 2430, 1280},
            {3040, 1440, 2810, 1330, 2450, 1280},
            {3120, 1440, 2940, 1330, 2470, 1280},
            {2048, 1536, 1870, 1430, 1760, 1130},
            {2560, 1600, 2310, 1470, 2220, 1250},
            {3840, 1644, 3520, 1550, 2980, 1450}
    };

    public static int[] getResolution(Context context) {
        int[] res = {0, 0};
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            manager.getDefaultDisplay().getRealMetrics(metric);
            res[0] = Math.max(metric.widthPixels, metric.heightPixels);
            res[1] = Math.min(metric.widthPixels, metric.heightPixels);
        }
        return res;
    }

    public static float getAspectRatio(Context context) {
        int[] res = getResolution(context);
        return res[1] == 0 ? 0 : 1f * res[0] / res[1];
    }
}