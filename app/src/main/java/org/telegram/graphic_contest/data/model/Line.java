package org.telegram.graphic_contest.data.model;

import org.telegram.graphic_contest.graph.util.ChartUtils;

import android.graphics.PathEffect;

import java.util.ArrayList;
import java.util.List;


public class Line {

    private static final int DEFAULT_LINE_STROKE_WIDTH_DP = 3;
    private static final int DEFAULT_POINT_RADIUS_DP = 1;
    private static final int UNINITIALIZED = 0;
    private int color;
    private int darkenColor;

    private int pointColor = UNINITIALIZED;

    private int strokeWidth = DEFAULT_LINE_STROKE_WIDTH_DP;
    private int pointRadius = DEFAULT_POINT_RADIUS_DP;
    private boolean hasGradientToTransparent;
    private boolean hasPoints = true;
    private boolean hasLines = true;
    private boolean hasLabels;
    private boolean hasLabelsOnlyForSelected;
    private boolean isCubic;
    private boolean isSquare;
    private boolean isFilled;
    private PathEffect pathEffect;
    private List<PointValue> values = new ArrayList<>();

    public Line() {

    }

    public Line(Line line) {
        this.color = line.color;
        this.pointColor = line.pointColor;
        this.darkenColor = line.darkenColor;
        this.strokeWidth = line.strokeWidth;
        this.pointRadius = line.pointRadius;
        this.hasGradientToTransparent = line.hasGradientToTransparent;
        this.hasPoints = line.hasPoints;
        this.hasLines = line.hasLines;
        this.hasLabels = line.hasLabels;
        this.hasLabelsOnlyForSelected = line.hasLabelsOnlyForSelected;
        this.isSquare = line.isSquare;
        this.isCubic = line.isCubic;
        this.isFilled = line.isFilled;
        this.pathEffect = line.pathEffect;

        for (final PointValue pointValue : line.values) {
            this.values.add(new PointValue(pointValue));
        }
    }

    public List<PointValue> getValues() {
        return this.values;
    }

    public void setValues(List<PointValue> values) {
        if (null == values) {
            this.values = new ArrayList<>();
        } else {
            this.values = values;
        }
    }

    public int getColor() {
        return color;
    }

    public Line setColor(final int color) {
        this.color = color;
        if (pointColor == UNINITIALIZED) {
            this.darkenColor = ChartUtils.darkenColor(color);
        }
        return this;
    }

    public int getDarkenColor() {
        return darkenColor;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public Line setStrokeWidth(final int strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public boolean hasPoints() {
        return hasPoints;
    }

    public boolean hasLines() {
        return hasLines;
    }

    public int getPointRadius() {
        return pointRadius;
    }

    public boolean isFilled() {
        return isFilled;
    }

    public PathEffect getPathEffect() {
        return pathEffect;
    }
}
