package org.telegram.graphic_contest.data.model;

public final class PointValue {

    private float x;
    private float y;
    private char[] label;


    public PointValue(final float x, final float y) {
        set(x, y);
    }

    public PointValue(final PointValue pointValue) {
        set(pointValue.x, pointValue.y);
        this.label = pointValue.label;
    }

    public PointValue set(final float x, final float y) {
        this.x = x;
        this.y = y;
        return this;
    }


    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }
}
