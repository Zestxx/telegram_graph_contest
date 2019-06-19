package org.telegram.graphic_contest.graph.computator;

import org.telegram.graphic_contest.data.model.Viewport;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;


public class ChartComputator {

    private static final float DEFAULT_MAXIMUM_ZOOM = 20f;
    private int chartWidth;
    private int chartHeight;

    private final Rect contentRectMinusAllMargins = new Rect();
    private final Rect contentRectMinusAxesMargins = new Rect();
    private final Rect maxContentRect = new Rect();

    private final Viewport currentViewport = new Viewport();
    private final Viewport maxViewport = new Viewport();
    private float minViewportWidth;
    private float minViewportHeight;
    private boolean isPreview;

    public void setContentRect(final int width, final int height, final int paddingLeft, final int paddingTop,
            int paddingRight,
            final int paddingBottom) {
        chartWidth = width;
        chartHeight = height;
        maxContentRect.set(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom);
        contentRectMinusAxesMargins.set(maxContentRect);
        contentRectMinusAllMargins.set(maxContentRect);
    }

    public void setIsPreview(final boolean preview) {
        isPreview = preview;
    }

    public void resetContentRect() {
        contentRectMinusAxesMargins.set(maxContentRect);
        contentRectMinusAllMargins.set(maxContentRect);
    }

    public void insetContentRect(final int deltaLeft, final int deltaTop, final int deltaRight, final int deltaBottom) {
        contentRectMinusAxesMargins.left = contentRectMinusAxesMargins.left + deltaLeft;
        contentRectMinusAxesMargins.top = contentRectMinusAxesMargins.top + deltaTop;
        contentRectMinusAxesMargins.right = contentRectMinusAxesMargins.right - deltaRight;
        contentRectMinusAxesMargins.bottom = contentRectMinusAxesMargins.bottom - deltaBottom;

        insetContentRectByInternalMargins(deltaLeft, deltaTop, deltaRight, deltaBottom);
    }

    public void insetContentRectByInternalMargins(final int deltaLeft, final int deltaTop, final int deltaRight,
            final int deltaBottom) {
        contentRectMinusAllMargins.left = contentRectMinusAllMargins.left + deltaLeft;
        contentRectMinusAllMargins.top = contentRectMinusAllMargins.top + deltaTop;
        contentRectMinusAllMargins.right = contentRectMinusAllMargins.right - deltaRight;
        contentRectMinusAllMargins.bottom = contentRectMinusAllMargins.bottom - deltaBottom;
    }

    public void setViewportTopLeft(float left, float top) {

        final float curWidth = currentViewport.width();
        final float curHeight = currentViewport.height();

        left = Math.max(maxViewport.left, Math.min(left, maxViewport.right - curWidth));
        top = Math.max(maxViewport.bottom + curHeight, Math.min(top, maxViewport.top));
        constrainViewport(left, top, left + curWidth, top - curHeight);
    }


    public float computeRawX(final float valueX) {
        if (isPreview) {
            return (valueX - currentViewport.left) * (chartWidth / currentViewport.width());
        } else {
            final float pixelOffset = (valueX - currentViewport.left) * (contentRectMinusAllMargins.width() /
                    currentViewport.width());

            return contentRectMinusAllMargins.left + pixelOffset;
        }
    }


    public float computeRawY(final float valueY) {
        final float pixelOffset = (valueY - currentViewport.bottom) * (contentRectMinusAllMargins.height() /
                currentViewport.height());
        return contentRectMinusAllMargins.bottom - pixelOffset;
    }

    public boolean rawPixelsToDataPoint(final float x, final float y, final PointF dest) {
        if (!contentRectMinusAllMargins.contains((int) x, (int) y)) {
            return true;
        }
        dest.set(currentViewport.left + (x - contentRectMinusAllMargins.left) * currentViewport.width() /
                        contentRectMinusAllMargins.width(),
                currentViewport.bottom + (y - contentRectMinusAllMargins.bottom) * currentViewport.height() /
                        -contentRectMinusAllMargins.height());
        return false;
    }


    public void computeScrollSurfaceSize(final Point out) {
        out.set((int) (maxViewport.width() * contentRectMinusAllMargins.width() / currentViewport.width()),
                (int) (maxViewport.height() * contentRectMinusAllMargins.height() / currentViewport.height()));
    }


    public boolean isWithinContentRect(final float x, final float y, final float precision) {
        if (x >= contentRectMinusAllMargins.left - precision && x <= contentRectMinusAllMargins.right + precision) {
            return y <= contentRectMinusAllMargins.bottom + precision && y >= contentRectMinusAllMargins.top -
                    precision;
        }
        return false;
    }


    public Rect getContentRectMinusAllMargins() {
        return contentRectMinusAllMargins;
    }


    public Rect getContentRectMinusAxesMargins() {
        return contentRectMinusAxesMargins;
    }


    public Viewport getCurrentViewport() {
        return currentViewport;
    }


    public void setCurrentViewport(final Viewport viewport) {
        constrainViewport(viewport.left, viewport.top, viewport.right, viewport.bottom);
    }


    public void setCurrentViewport(final float left, final float top, final float right, final float bottom) {
        constrainViewport(left, top, right, bottom);
    }


    public Viewport getMaximumViewport() {
        return maxViewport;
    }


    public void setMaxViewport(final Viewport maxViewport) {
        setMaxViewport(maxViewport.left, maxViewport.top, maxViewport.right, maxViewport.bottom);
    }

    public Viewport getVisibleViewport() {
        return currentViewport;
    }

    public int getChartWidth() {
        return chartWidth;
    }

    public int getChartHeight() {
        return chartHeight;
    }


    private void setMaxViewport(final float left, final float top, final float right, final float bottom) {
        this.maxViewport.set(left, top, right, bottom);
        computeMinimumWidthAndHeight();
    }

    private void computeMinimumWidthAndHeight() {
        final float maxZoom = DEFAULT_MAXIMUM_ZOOM;
        minViewportWidth = this.maxViewport.width() / maxZoom;
        minViewportHeight = this.maxViewport.height() / maxZoom;
    }

    private void constrainViewport(float left, float top, float right, float bottom) {

        if (right - left < minViewportWidth) {

            right = left + minViewportWidth;
            if (left < maxViewport.left) {
                left = maxViewport.left;
                right = left + minViewportWidth;
            } else if (right > maxViewport.right) {
                right = maxViewport.right;
                left = right - minViewportWidth;
            }
        }

        if (top - bottom < minViewportHeight) {

            bottom = top - minViewportHeight;
            if (top > maxViewport.top) {
                top = maxViewport.top;
                bottom = top - minViewportHeight;
            } else if (bottom < maxViewport.bottom) {
                bottom = maxViewport.bottom;
                top = bottom + minViewportHeight;
            }
        }

        currentViewport.left = Math.max(maxViewport.left, left);
        currentViewport.top = Math.min(maxViewport.top, top);
        currentViewport.right = Math.min(maxViewport.right, right);
        currentViewport.bottom = Math.max(maxViewport.bottom, bottom);

    }
}