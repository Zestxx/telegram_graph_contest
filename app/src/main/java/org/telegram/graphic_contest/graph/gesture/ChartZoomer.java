package org.telegram.graphic_contest.graph.gesture;

import org.telegram.graphic_contest.data.model.Viewport;
import org.telegram.graphic_contest.graph.computator.ChartComputator;

import android.graphics.PointF;
import android.os.SystemClock;
import android.view.MotionEvent;


public class ChartZoomer {

    private static final float ZOOM_AMOUNT = 0.25f;

    private boolean mFinished = true;
    private long mStartRTC;
    private ZoomType zoomType;
    private final PointF zoomFocalPoint = new PointF();
    private final PointF viewportFocus = new PointF();
    private final Viewport scrollerStartViewport = new Viewport();

    boolean startZoom(final MotionEvent e, final ChartComputator computator) {
        forceFinished();
        scrollerStartViewport.set(computator.getCurrentViewport());
        if (computator.rawPixelsToDataPoint(e.getX(), e.getY(), zoomFocalPoint)) {
            return false;
        }
        startZoom();
        return true;
    }

    boolean computeZoom(final ChartComputator computator) {
        if (computeZoom()) {
            final float newWidth = 1.0f * scrollerStartViewport.width();
            final float newHeight = 1.0f * scrollerStartViewport.height();
            final float pointWithinViewportX = (zoomFocalPoint.x - scrollerStartViewport.left)
                    / scrollerStartViewport.width();
            final float pointWithinViewportY = (zoomFocalPoint.y - scrollerStartViewport.bottom)
                    / scrollerStartViewport.height();

            final float left = zoomFocalPoint.x - newWidth * pointWithinViewportX;
            final float top = zoomFocalPoint.y + newHeight * (1 - pointWithinViewportY);
            final float right = zoomFocalPoint.x + newWidth * (1 - pointWithinViewportX);
            final float bottom = zoomFocalPoint.y - newHeight * pointWithinViewportY;
            setCurrentViewport(computator, left, top, right, bottom);
            return true;
        }
        return false;
    }

    boolean scale(final ChartComputator computator, final float focusX, final float focusY, final float scale) {

        final float newWidth = scale * computator.getCurrentViewport().width();
        final float newHeight = scale * computator.getCurrentViewport().height();

        final float left = viewportFocus.x - (focusX - computator.getContentRectMinusAllMargins().left)
                * (newWidth / computator.getContentRectMinusAllMargins().width());

        final float top = viewportFocus.y + (focusY - computator.getContentRectMinusAllMargins().top)
                * (newHeight / computator.getContentRectMinusAllMargins().height());

        final float right = left + newWidth;
        final float bottom = top - newHeight;
        setCurrentViewport(computator, left, top, right, bottom);
        return true;
    }

    public void scale(final ChartComputator computator, final float left, final float top, final float right,
            final float bottom) {
        computator.setCurrentViewport(left, top, right, bottom);
    }

    private void setCurrentViewport(final ChartComputator computator, final float left, final float top,
            final float right, final float bottom) {
        final Viewport currentViewport = computator.getCurrentViewport();
        if (ZoomType.HORIZONTAL_AND_VERTICAL == zoomType) {
            computator.setCurrentViewport(left, top, right, bottom);
        } else if (ZoomType.HORIZONTAL == zoomType) {
            computator.setCurrentViewport(left, currentViewport.top, right, currentViewport.bottom);
        } else if (ZoomType.VERTICAL == zoomType) {
            computator.setCurrentViewport(currentViewport.left, top, currentViewport.right, bottom);
        }
    }

    void setZoomType(final ZoomType zoomType) {
        this.zoomType = zoomType;
    }


    private void forceFinished() {
        mFinished = true;
    }


    private void startZoom() {
        mStartRTC = SystemClock.elapsedRealtime();
        mFinished = false;
    }


    private boolean computeZoom() {
        if (mFinished) {
            return false;
        }

        final long tRTC = SystemClock.elapsedRealtime() - mStartRTC;
        if (tRTC >= 0) {
            mFinished = true;
            return false;
        }
        return true;
    }
}