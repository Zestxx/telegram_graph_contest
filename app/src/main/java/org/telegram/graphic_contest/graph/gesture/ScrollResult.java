package org.telegram.graphic_contest.graph.gesture;

public class ScrollResult {

    private final boolean canScrollX;
    private final boolean canScrollY;

    ScrollResult(final boolean canScrollX, final boolean canScrollY) {
        super();
        this.canScrollX = canScrollX;
        this.canScrollY = canScrollY;
    }

    boolean isCanScrollX() {
        return canScrollX;
    }

    boolean isCanScrollY() {
        return canScrollY;
    }
}
