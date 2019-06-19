package org.telegram.graphic_contest.graph.gesture;

import org.telegram.graphic_contest.data.model.Viewport;
import org.telegram.graphic_contest.graph.computator.ChartComputator;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.widget.ScrollerCompat;


class ChartScroller {

    private final Viewport scrollerStartViewport = new Viewport();
    private final Point surfaceSizeBuffer = new Point();
    private final ScrollerCompat scroller;

    ChartScroller(final Context context) {
        scroller = ScrollerCompat.create(context);
    }

    boolean startScroll(final ChartComputator computator) {
        scroller.abortAnimation();
        scrollerStartViewport.set(computator.getCurrentViewport());
        return true;
    }

    ScrollResult scroll(final ChartComputator computator, final float distanceX, final float distanceY) {

        final Viewport maxViewport = computator.getMaximumViewport();
        final Viewport visibleViewport = computator.getVisibleViewport();
        final Viewport currentViewport = computator.getCurrentViewport();
        final Rect contentRect = computator.getContentRectMinusAllMargins();

        final boolean canScrollLeft = currentViewport.left > maxViewport.left;
        final boolean canScrollRight = currentViewport.right < maxViewport.right;
        final boolean canScrollTop = currentViewport.top < maxViewport.top;
        final boolean canScrollBottom = currentViewport.bottom > maxViewport.bottom;

        boolean canScrollX = false;
        boolean canScrollY = false;

        if (canScrollLeft && distanceX <= 0) {
            canScrollX = true;
        } else if (canScrollRight && distanceX >= 0) {
            canScrollX = true;
        }

        if (canScrollTop && distanceY <= 0) {
            canScrollY = true;
        } else if (canScrollBottom && distanceY >= 0) {
            canScrollY = true;
        }

        if (canScrollX || canScrollY) {

            computator.computeScrollSurfaceSize(surfaceSizeBuffer);

            final float viewportOffsetX = distanceX * visibleViewport.width() / contentRect.width();
            final float viewportOffsetY = 0;

            computator.setViewportTopLeft(currentViewport.left + viewportOffsetX,
                    currentViewport.top + viewportOffsetY);
        }

        return new ScrollResult(canScrollX, canScrollY);
    }

    boolean computeScrollOffset(final ChartComputator computator) {
        if (scroller.computeScrollOffset()) {

            final Viewport maxViewport = computator.getMaximumViewport();

            computator.computeScrollSurfaceSize(surfaceSizeBuffer);

            final float currXRange = maxViewport.left + maxViewport.width() * scroller.getCurrX() /
                    surfaceSizeBuffer.x;
            final float currYRange = maxViewport.top - maxViewport.height() * scroller.getCurrY() /
                    surfaceSizeBuffer.y;

            computator.setViewportTopLeft(currXRange, currYRange);

            return true;
        }

        return false;
    }

    boolean fling(final int velocityX, final int velocityY, final ChartComputator computator) {

        computator.computeScrollSurfaceSize(surfaceSizeBuffer);
        scrollerStartViewport.set(computator.getCurrentViewport());

        final int startX = (int) (surfaceSizeBuffer.x * (scrollerStartViewport.left - computator.getMaximumViewport().left)
                / computator.getMaximumViewport().width());

        final int startY = (int) (surfaceSizeBuffer.y * (computator.getMaximumViewport().top - scrollerStartViewport.top) /
                computator.getMaximumViewport().height());

        scroller.abortAnimation();

        final int width = computator.getContentRectMinusAllMargins().width();
        final int height = computator.getContentRectMinusAllMargins().height();
        scroller.fling(startX, startY, velocityX, velocityY, 0, surfaceSizeBuffer.x - width + 1, 0,
                surfaceSizeBuffer.y - height + 1);
        return true;
    }
}
