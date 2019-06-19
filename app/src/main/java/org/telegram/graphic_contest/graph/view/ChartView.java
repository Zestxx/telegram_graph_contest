package org.telegram.graphic_contest.graph.view;

import org.telegram.graphic_contest.data.PreviewPosition;
import org.telegram.graphic_contest.data.model.ChartData;
import org.telegram.graphic_contest.data.model.Line;
import org.telegram.graphic_contest.data.model.PointValue;
import org.telegram.graphic_contest.data.model.Viewport;
import org.telegram.graphic_contest.graph.computator.ChartComputator;
import org.telegram.graphic_contest.graph.gesture.ChartTouchHandler;
import org.telegram.graphic_contest.graph.gesture.ScrollResult;
import org.telegram.graphic_contest.graph.gesture.ZoomType;
import org.telegram.graphic_contest.graph.provider.ChartDataProvider;
import org.telegram.graphic_contest.graph.renderer.AxesRenderer;
import org.telegram.graphic_contest.graph.renderer.ChartRenderer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChartView extends View implements ChartDataProvider, ChartTouchHandler.ScrollerListener {

    private static final int ONE_HUNDRED_PERCENT = 100;
    private static final int ANIMATION_DURATION = 500;

    protected ChartComputator chartComputator;
    protected AxesRenderer axesRenderer;
    protected ChartTouchHandler touchHandler;
    protected ChartRenderer chartRenderer;
    protected boolean isInteractive = true;
    protected ChartData data;
    private ValueAnimator valueAnimatorToMax;
    private float lastMaxValueY;

    public ChartView(final Context context) {
        this(context, null, 0);
        init(context);
    }

    public ChartView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public ChartView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        chartComputator = new ChartComputator();
        touchHandler = new ChartTouchHandler(context, this);
        touchHandler.setScrollerListener(this);
        axesRenderer = new AxesRenderer(context, this);

        valueAnimatorToMax = ValueAnimator.ofFloat().setDuration(ANIMATION_DURATION);
        final ValueAnimator.AnimatorUpdateListener animatorUpdateListener
                = new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final Viewport currentViewport = chartComputator.getVisibleViewport();

                touchHandler.getChartZoomer().scale(
                        chartComputator,
                        currentViewport.left,
                        (float) animation.getAnimatedValue(),
                        currentViewport.right,
                        0
                );
                ViewCompat.postInvalidateOnAnimation(ChartView.this);
            }
        };
        valueAnimatorToMax.addUpdateListener(animatorUpdateListener);
        valueAnimatorToMax.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                super.onAnimationEnd(animation);
                final Viewport currentViewport = chartComputator.getVisibleViewport();
                if ((int) currentViewport.top != (int) lastMaxValueY) {
                    valueAnimatorToMax.setFloatValues(currentViewport.top, lastMaxValueY);
                    valueAnimatorToMax.start();
                }
            }
        });

        setChartRenderer(new ChartRenderer(context, this, this));
    }

    @Override
    public void onScrollDataChanged(final ScrollResult scrollResult) {
        changeViewportWithAnimationIfNeed(getChartComputator().getVisibleViewport());
    }

    public void setPositionByPreview(final PreviewPosition previewPosition) {
        final int chartWidth = (int) chartComputator.getMaximumViewport().right;
        final float left = chartWidth / (float) ONE_HUNDRED_PERCENT * previewPosition.getPositionInPercent();
        final float right = left + chartWidth / (float) ONE_HUNDRED_PERCENT * previewPosition.getSizeInPercent();
        final Viewport viewport = new Viewport();
        final Viewport currentViewport = getCurrentViewport();

        viewport.set(left, currentViewport.top, right, 10);
        setCurrentViewport(viewport);
        changeViewportWithAnimationIfNeed(getChartComputator().getVisibleViewport());
    }

    public void updateWithAnimation() {
        changeViewportWithAnimationIfNeed(getChartComputator().getVisibleViewport());
    }

    private void changeViewportWithAnimationIfNeed(final Viewport visibleViewport) {

        final List<Line> lines = getChartData().getLines();
        final int elementsCount = ((int) visibleViewport.right - (int) visibleViewport.left) * lines.size();
        final List<Float> elements = new ArrayList<>(elementsCount);

        final Viewport currentViewport = getCurrentViewport();
        for (int i = 0; i < lines.size(); i++) {
            final List<PointValue> values = lines.get(i).getValues();

            if (!values.isEmpty()) {
                for (int j = (int) visibleViewport.left + 1; j < visibleViewport.right - 1; j++) {
                    elements.add(values.get(j).getY());
                }
            }
        }

        if (!elements.isEmpty()) {
            lastMaxValueY = Collections.max(elements);
        }

        if (!valueAnimatorToMax.isStarted()) {
            valueAnimatorToMax.setFloatValues(currentViewport.top, lastMaxValueY);
            valueAnimatorToMax.start();
        }
    }

    @Override
    protected void onSizeChanged(final int width, final int height, final int oldWidth, final int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        chartComputator.setContentRect(getWidth(), getHeight(), getPaddingLeft(), getPaddingTop(), getPaddingRight(),
                getPaddingBottom());
        chartRenderer.onChartSizeChanged();
        axesRenderer.onChartSizeChanged();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if (isInteractive) {
            axesRenderer.drawInBackground(canvas);
        }
        chartRenderer.draw(canvas);
        if (isInteractive) {
            chartRenderer.drawTouched(canvas);
            axesRenderer.drawInForeground(canvas);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        super.onTouchEvent(event);

        if (isInteractive) {

            final boolean needInvalidate = touchHandler.handleTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                final Viewport currentViewport = chartComputator.getVisibleViewport();
                if ((int) currentViewport.top != (int) lastMaxValueY) {
                    valueAnimatorToMax.setFloatValues(currentViewport.top, lastMaxValueY);
                    valueAnimatorToMax.start();
                }
            }

            if (needInvalidate) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (isInteractive) {
            if (touchHandler.computeScroll()) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }

    @Override
    public ChartData getChartData() {
        return data;
    }

    @Override
    public void setChartData(final ChartData data) {
        if (data != null) {
            this.data = data;
            onChartDataChange();
            for (final Line line : getChartData().getLines()) {
                line.getValues().remove(0);
            }
        }
    }

    public void setGestureIsEnable(final boolean gestureIsEnable) {
        touchHandler.setGestureIsEnable(gestureIsEnable);
    }

    public ChartRenderer getChartRenderer() {
        return chartRenderer;
    }

    public void setChartRenderer(final ChartRenderer renderer) {
        chartRenderer = renderer;
        resetRendererAndTouchHandler();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public ChartComputator getChartComputator() {
        return chartComputator;
    }

    public void setInteractive(final boolean isInteractive) {
        this.isInteractive = isInteractive;
        chartComputator.setIsPreview(!isInteractive);
    }

    public void setZoomType(final ZoomType zoomType) {
        touchHandler.setZoomType(zoomType);
    }

    public Viewport getMaximumViewport() {
        return chartRenderer.getMaximumViewport();
    }

    public Viewport getCurrentViewport() {
        return getChartRenderer().getCurrentViewport();
    }

    public void setCurrentViewport(final Viewport targetViewport) {
        if (targetViewport != null) {
            chartRenderer.setCurrentViewport(targetViewport);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    protected void onChartDataChange() {
        chartComputator.resetContentRect();
        chartRenderer.onChartDataChanged();
        axesRenderer.onChartDataChanged();
        ViewCompat.postInvalidateOnAnimation(this);
    }


    protected void resetRendererAndTouchHandler() {
        this.chartRenderer.resetRenderer();
        this.axesRenderer.resetRenderer();
        this.touchHandler.resetTouchHandler();
    }


    @Override
    public boolean canScrollHorizontally(final int direction) {
        final Viewport currentViewport = getCurrentViewport();
        final Viewport maximumViewport = getMaximumViewport();
        if (direction < 0) {
            return currentViewport.left > maximumViewport.left;
        } else {
            return currentViewport.right < maximumViewport.right;
        }
    }
}
