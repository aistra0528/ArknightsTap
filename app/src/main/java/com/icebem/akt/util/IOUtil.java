package com.icebem.akt.util;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class IOUtil {
    public static InputStream fromAssets(Context context, String path) throws IOException {
        return context.getAssets().open(path);
    }

    public static InputStream fromWeb(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.disconnect();
        return connection.getInputStream();
    }

    public static String stream2String(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (in.read(buffer) != -1)
            out.write(buffer);
        in.close();
        out.flush();
        out.close();
        return new String(out.toByteArray());
    }
}