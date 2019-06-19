package org.telegram.graphic_contest.data.model;

import java.util.ArrayList;
import java.util.List;


public class ChartData {

    private Axis axisXBottom;
    private Axis axisYLeft;
    private static final float DEFAULT_BASE_VALUE = 0.0f;

    private boolean hideJoined;
    private boolean hideLeft;

    private List<Line> lines = new ArrayList<>();

    public void setHideJoined(final boolean hideJoined) {
        this.hideJoined = hideJoined;
    }

    public void setHideLeft(final boolean hideLeft) {
        this.hideLeft = hideLeft;
    }

    public ChartData(final List<Line> lines) {
        setLines(lines);
    }

    public Axis getAxisXBottom() {
        return axisXBottom;
    }


    public void setAxisXBottom(final Axis axisX) {
        this.axisXBottom = axisX;
    }


    public Axis getAxisYLeft() {
        return axisYLeft;
    }


    public void setAxisYLeft(final Axis axisY) {
        this.axisYLeft = axisY;
    }


    public List<Line> getLines() {
        if (hideJoined && !hideLeft) {
            return lines.subList(1, 2);
        } else if (hideLeft && !hideJoined) {
            return lines.subList(0, 1);
        } else if (hideJoined && hideLeft) {
            return lines.subList(0, 0);
        } else {
            return lines;
        }
    }

    private void setLines(final List<Line> lines) {
        if (lines == null) {
            this.lines = new ArrayList<>();
        } else {
            this.lines = lines;
        }
    }


    public float getBaseValue() {
        return DEFAULT_BASE_VALUE;
    }
}