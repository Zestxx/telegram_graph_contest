package org.telegram.graphic_contest.data.model;

public class HighLightPointParams {

    private final float positionX;
    private final float positionY;
    private final int highLightColor;
    private final int pointRadius;
    private final PointValue pointValue;
    private final AxisValue pointAxisValue;

    public HighLightPointParams(final float positionX, final float positionY, final int highLightColor,
            final int pointRadius, final PointValue pointValue,
            final AxisValue pointAxisValue) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.highLightColor = highLightColor;
        this.pointRadius = pointRadius;
        this.pointValue = pointValue;
        this.pointAxisValue = pointAxisValue;
    }

    public float getPositionX() {
        return positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public int getPointColor() {
        return highLightColor;
    }

    public int getPointRadius() {
        return pointRadius;
    }

    public PointValue getPointValue() {
        return pointValue;
    }

    public AxisValue getPointAxisValue() {
        return pointAxisValue;
    }
}
