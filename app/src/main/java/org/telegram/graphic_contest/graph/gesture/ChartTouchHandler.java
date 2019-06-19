package org.telegram.graphic_contest.graph.gesture;

import org.telegram.graphic_contest.data.model.SelectedValue;
import org.telegram.graphic_contest.graph.computator.ChartComputator;
import org.telegram.graphic_contest.graph.renderer.ChartRenderer;
import org.telegram.graphic_contest.graph.view.ChartView;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;


public class ChartTouchHandler {

    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;
    private final ChartScroller chartScroller;
    private final ChartZoomer chartZoomer;
    private final ChartView chartView;
    private ChartComputator computator;
    private ChartRenderer renderer;

    private final boolean isZoomEnabled = true;
    private final boolean isScrollEnabled = true;
    private ScrollerListener scrollerListener;

    private final SelectedValue selectedValue = new SelectedValue();
    private final SelectedValue oldSelectedValue = new SelectedValue();
    private boolean gestureIsEnable = true;

    public ChartTouchHandler(final Context context, final ChartView chartView) {
        this.chartView = chartView;
        this.computator = chartView.getChartComputator();
        this.renderer = chartView.getChartRenderer();
        gestureDetector = new GestureDetector(context, new ChartGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ChartScaleGestureListener());
        chartScroller = new ChartScroller(context);
        chartZoomer = new ChartZoomer();
    }

    public void resetTouchHandler() {
        this.computator = chartView.getChartComputator();
        this.renderer = chartView.getChartRenderer();
    }

    public void setScrollerListener(
            final ScrollerListener scrollerListener) {
        this.scrollerListener = scrollerListener;
    }


    public boolean computeScroll() {
        boolean needInvalidate = false;
        if (isScrollEnabled && chartScroller.computeScrollOffset(computator)) {
            needInvalidate = true;
        }
        if (isZoomEnabled && chartZoomer.computeZoom(computator)) {
            needInvalidate = true;
        }
        return needInvalidate;
    }

    public ChartZoomer getChartZoomer() {
        return chartZoomer;
    }


    public boolean handleTouchEvent(final MotionEvent event) {
        boolean needInvalidate = false;
        if (gestureIsEnable) {
            needInvalidate = gestureDetector.onTouchEvent(event);

            needInvalidate = scaleGestureDetector.onTouchEvent(event) || needInvalidate;
        }

        needInvalidate = computeTouch(event) || needInvalidate;

        return needInvalidate;
    }

    private boolean computeTouch(final MotionEvent event) {
        boolean needInvalidate = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                final boolean wasTouched = renderer.isTouched();
                final boolean isTouched = checkTouch(event.getX(), event.getY());
                if (wasTouched != isTouched) {
                    needInvalidate = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (renderer.isTouched()) {
                    needInvalidate = true;
                }
                break;
        }
        return needInvalidate;
    }

    private boolean checkTouch(final float touchX, final float touchY) {
        oldSelectedValue.set(selectedValue);
        selectedValue.clear();

        if (renderer.checkTouch(touchX, touchY)) {
            selectedValue.set(renderer.getSelectedValue());
        }

        if (oldSelectedValue.isSet() && selectedValue.isSet() && !oldSelectedValue.equals(selectedValue)) {
            return false;
        } else {
            return renderer.isTouched();
        }
    }

    public void setZoomType(final ZoomType zoomType) {
        chartZoomer.setZoomType(zoomType);
    }

    protected class ChartScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(final ScaleGestureDetector detector) {
            if (isZoomEnabled) {
                float scale = 2.0f - detector.getScaleFactor();
                if (Float.isInfinite(scale)) {
                    scale = 1;
                }
                return chartZoomer.scale(computator, detector.getFocusX(), detector.getFocusY(), scale);
            }

            return false;
        }
    }

    protected class ChartGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(final MotionEvent e) {
            if (isScrollEnabled) {
                return chartScroller.startScroll(computator);
            }
            return false;

        }

        @Override
        public boolean onDoubleTap(final MotionEvent e) {
            if (isZoomEnabled) {
                return chartZoomer.startZoom(e, computator);
            }

            return false;
        }

        @Override
        public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX,
                final float distanceY) {
            if (isScrollEnabled) {
                final ScrollResult scrollResult = chartScroller
                        .scroll(computator, distanceX, distanceY);

                if (scrollerListener != null) {
                    scrollerListener.onScrollDataChanged(scrollResult);
                }
                return scrollResult.isCanScrollX() || scrollResult.isCanScrollY();
            }

            return false;

        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX,
                final float velocityY) {
            if (isScrollEnabled) {
                return chartScroller.fling((int) -velocityX, (int) -velocityY, computator);
            }

            return false;
        }
    }

    public void setGestureIsEnable(final boolean gestureIsEnable) {
        this.gestureIsEnable = gestureIsEnable;
    }

    public interface ScrollerListener {

        void onScrollDataChanged(ScrollResult scrollResult);
    }
}
