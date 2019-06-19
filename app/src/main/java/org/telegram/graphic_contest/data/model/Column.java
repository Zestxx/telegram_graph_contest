package org.telegram.graphic_contest.data.model;

import java.util.List;

public class Column {

    private String name;
    private List<Long> values;

    public Column(final String name, final List<Long> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<Long> getValues() {
        return values;
    }

    public void setValues(final List<Long> values) {
        this.values = values;
    }
}
