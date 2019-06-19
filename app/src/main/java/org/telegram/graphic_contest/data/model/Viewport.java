package org.telegram.graphic_contest.data.model;

public class Viewport {

    public float left;
    public float top;
    public float right;
    public float bottom;

    public final float width() {
        return right - left;
    }


    public final float height() {
        return top - bottom;
    }

    public void set(final float left, final float top, final float right, final float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }


    public void set(final Viewport src) {
        this.left = src.left;
        this.top = src.top;
        this.right = src.right;
        this.bottom = src.bottom;
    }
}
