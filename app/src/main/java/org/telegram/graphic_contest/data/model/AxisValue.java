package org.telegram.graphic_contest.data.model;

public class AxisValue {

    private final float value;
    private char[] label;
    private final long time;

    public AxisValue(final float value, final long time) {
        this.value = value;
        this.time = time;
    }

    public float getValue() {
        return value;
    }

    public void setLabel(final String label) {
        this.label = label.toCharArray();
    }

    public char[] getLabelAsChars() {
        return label;
    }


    @Deprecated
    public AxisValue setLabel(final char[] label) {
        this.label = label;
        return this;
    }

    public long getTime() {
        return time;
    }
}