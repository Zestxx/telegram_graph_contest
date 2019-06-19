package org.telegram.graphic_contest.data.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.telegram.graphic_contest.data.ColumnDeserializer;
import org.telegram.graphic_contest.data.model.Column;

public final class GsonUtil {

    private static final Gson ourInstance = new GsonBuilder()
            .registerTypeAdapter(Column.class, new ColumnDeserializer())
            .create();

    private GsonUtil() {
    }

    public static Gson getGson() {
        return ourInstance;
    }
}
