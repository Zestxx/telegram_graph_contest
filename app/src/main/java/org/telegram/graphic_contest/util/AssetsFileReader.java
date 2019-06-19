package org.telegram.graphic_contest.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AssetsFileReader {

    private AssetManager assets;

    public AssetsFileReader(Context context) {
        assets = context.getAssets();
    }

    public String getString(final String fileName) {
        String json = null;
        try {
            final InputStream inputStream = assets.open(fileName);
            final int size = inputStream.available();
            final byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
        return json;
    }
}