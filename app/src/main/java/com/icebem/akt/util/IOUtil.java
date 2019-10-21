package com.icebem.akt.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class IOUtil {
    public static InputStream fromAssets(Context context, String path) throws IOException {
        return context.getAssets().open(path);
    }

    public static String stream2String(InputStream in) throws IOException {
        byte[] bytes = new byte[in.available()];
        int len = in.read(bytes);
        return new String(bytes);
    }
}