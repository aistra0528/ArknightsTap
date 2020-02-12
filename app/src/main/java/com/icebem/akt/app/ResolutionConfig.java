package com.icebem.akt.app;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ResolutionConfig {
    //    public static final float RATIO_MAX = 2.5f;
    //    public static final float RATIO_MIN = 1.25f;
    //    public static final float RATIO_DEFAULT = 1.77f;

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

//    public static float getAspectRatio(Context context) {
//        int[] res = getResolution(context);
//        return res[1] == 0 ? 0 : 1f * res[0] / res[1];
//    }
}