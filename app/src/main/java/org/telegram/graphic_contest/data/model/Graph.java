
package org.telegram.graphic_contest.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Graph {

    @SerializedName("columns")
    private List<Column> columns;

    @SerializedName("types")
    private Map<String, String> types;

    @SerializedName("names")
    private Map<String, String> names;

    @SerializedName("colors")
    private Map<String, String> colors;

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(final List<Column> columns) {
        this.columns = columns;
    }

    public Map<String, String> getTypes() {
        return types;
    }

    public void setTypes(final Map<String, String> types) {
        this.types = types;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(final Map<String, String> names) {
        this.names = names;
    }

    public Map<String, String> getColors() {
        return colors;
    }

    public void setColors(final Map<String, String> colors) {
        this.colors = colors;
    }
}
