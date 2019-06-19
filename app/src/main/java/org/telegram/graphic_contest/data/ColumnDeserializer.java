package org.telegram.graphic_contest.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.telegram.graphic_contest.data.model.Column;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ColumnDeserializer implements JsonDeserializer {

    @Override
    public Object deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException {
        final JsonArray jsonArray = json.getAsJsonArray();
        final String name = jsonArray.get(0).getAsString();
        final List<Long> values = new ArrayList<>();
        for (int i = 1; i < jsonArray.size() - 1; i++) {
            values.add(jsonArray.get(i).getAsLong());
        }
        return new Column(name, values);
    }
}
