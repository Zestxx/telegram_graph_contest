package org.telegram.graphic_contest.data;

public class PreviewPosition {

    private final float sizeInPercent;
    private final float positionInPercent;

    public PreviewPosition(final float sizeInPercent, final float positionInPercent) {
        this.sizeInPercent = sizeInPercent;
        this.positionInPercent = positionInPercent;
    }

    public float getSizeInPercent() {
        return sizeInPercent;
    }

    public float getPositionInPercent() {
        return positionInPercent;
    }
}
