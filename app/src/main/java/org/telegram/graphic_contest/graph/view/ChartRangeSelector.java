package org.telegram.graphic_contest.graph.view;

import org.telegram.graphic_contest.R;
import org.telegram.graphic_contest.data.PreviewPosition;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class ChartRangeSelector extends FrameLayout {

    private static final int DEFAULT_SELECTED_RANGE_SIZE = 400;
    private static final int RANGE_SELECTOR_VERTICAL_BORDERS_WIDTH = 16;
    private static final int RANGE_SELECTOR_HORIZONTAL_BORDERS_WIDTH = 5;

    final Path selectorBordersPath = new Path();

    private Paint overlayPaint;
    private Paint rangeSelectorBorderPaint;
    private boolean selectorMoveIsStarted;
    private boolean leftBorderMoveIsStarted;
    private boolean rightBorderMoveIsStarted;

    private PointF touchStartPoint = new PointF(0F, 0F);
    private Rect rangeSelectorScrollStartPosition;
    private final Rect rangeSelectorPosition = new Rect();
    private final RectF rangeSelectorLeftBorder = new RectF();
    private final RectF rangeSelectorRightBorder = new RectF();
    private PreviewPosition currentPreviewPosition = new PreviewPosition(0, 0);
    private OnSelectorListener onSelectorListener;

    public ChartRangeSelector(final Context context) {
        super(context);
        init();
    }

    public ChartRangeSelector(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChartRangeSelector(final Context context, @Nullable final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ChartRangeSelector(final Context context, @Nullable final AttributeSet attrs,
            final int defStyleAttr,
            final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setOnSelectorListener(final OnSelectorListener onSelectorListener) {
        this.onSelectorListener = onSelectorListener;
    }

    public PreviewPosition getCurrentPreviewPosition() {
        return currentPreviewPosition;
    }

    public void setOverlayColor(final int overlayColor) {
        overlayPaint.setColor(overlayColor);
        invalidate();
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setSelectorInitialPosition();
        onRangeChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                touchStartPoint = new PointF(event.getX(), event.getY());
                rangeSelectorScrollStartPosition = new Rect();
                rangeSelectorScrollStartPosition.set(rangeSelectorPosition);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                handleRangeSelectorTouch(event);
                onRangeChanged();
                break;
            }
            case MotionEvent.ACTION_UP: {
                selectorMoveIsStarted = false;
                leftBorderMoveIsStarted = false;
                rightBorderMoveIsStarted = false;
                touchStartPoint = new PointF(0F, 0F);
                break;
            }
        }
        invalidate();
        return true;
    }

    @Override
    protected void dispatchDraw(final Canvas canvas) {
        super.dispatchDraw(canvas);
        drawOverlay(canvas);
        drawSelector(canvas);
    }


    public PreviewPosition getSelectedGraphRange() {
        final float positionInPercent = 100F * rangeSelectorLeftBorder.left / getWidth();
        final float selectedSizeInPercent = 100F * (rangeSelectorRightBorder.right - rangeSelectorLeftBorder.left)
                / getWidth();
        return new PreviewPosition(selectedSizeInPercent, positionInPercent);
    }

    private void init() {
        overlayPaint = new Paint();
        overlayPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_range_selector_overlay));

        rangeSelectorBorderPaint = new Paint();
        rangeSelectorBorderPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_range_selector_border));
    }

    private void setSelectorInitialPosition() {
        rangeSelectorPosition.set(getWidth() - DEFAULT_SELECTED_RANGE_SIZE, 0, getWidth(), getHeight());

        rangeSelectorLeftBorder.set(rangeSelectorPosition.left, 0,
                rangeSelectorPosition.left + RANGE_SELECTOR_VERTICAL_BORDERS_WIDTH, getHeight());

        rangeSelectorRightBorder.set(rangeSelectorPosition.right - RANGE_SELECTOR_VERTICAL_BORDERS_WIDTH,
                0, rangeSelectorPosition.right, getHeight());
        invalidate();
    }

    private void drawOverlay(final Canvas canvas) {
        canvas.drawRect(0F, 0F, (float) rangeSelectorPosition.left, (float) getHeight(), overlayPaint);
        canvas.drawRect(rangeSelectorPosition.right, 0F, (float) getWidth(), (float) getHeight(), overlayPaint);
    }

    private void drawSelector(final Canvas canvas) {
        selectorBordersPath.rewind();


        rangeSelectorLeftBorder.set(
                rangeSelectorPosition.left,
                0,
                rangeSelectorPosition.left + RANGE_SELECTOR_VERTICAL_BORDERS_WIDTH,
                getHeight()
        );
        selectorBordersPath.addRect(rangeSelectorLeftBorder, Path.Direction.CW);


        rangeSelectorRightBorder.set(
                rangeSelectorPosition.right - RANGE_SELECTOR_VERTICAL_BORDERS_WIDTH,
                0,
                rangeSelectorPosition.right,
                getHeight()
        );
        selectorBordersPath.addRect(rangeSelectorRightBorder, Path.Direction.CW);


        selectorBordersPath.addRect(
                rangeSelectorPosition.left + RANGE_SELECTOR_VERTICAL_BORDERS_WIDTH,
                0,
                rangeSelectorPosition.right - RANGE_SELECTOR_VERTICAL_BORDERS_WIDTH,
                RANGE_SELECTOR_HORIZONTAL_BORDERS_WIDTH,
                Path.Direction.CW
        );


        selectorBordersPath.addRect(
                rangeSelectorPosition.left + RANGE_SELECTOR_VERTICAL_BORDERS_WIDTH,
                getHeight() - RANGE_SELECTOR_HORIZONTAL_BORDERS_WIDTH,
                rangeSelectorPosition.right - RANGE_SELECTOR_VERTICAL_BORDERS_WIDTH,
                getHeight(),
                Path.Direction.CW
        );

        canvas.drawPath(selectorBordersPath, rangeSelectorBorderPaint);
    }

    private void handleRangeSelectorTouch(final MotionEvent event) {
        final RectF leftBorderTouchRect = new RectF(rangeSelectorLeftBorder);
        leftBorderTouchRect.left -= 20;
        leftBorderTouchRect.right += 20;
        final RectF rightBorderTouchRect = new RectF(rangeSelectorRightBorder);
        rightBorderTouchRect.left -= 20;
        rightBorderTouchRect.right += 20;

        if ((leftBorderTouchRect.contains(touchStartPoint.x, touchStartPoint.y)
                && !selectorMoveIsStarted)
                || leftBorderMoveIsStarted && !selectorMoveIsStarted) {

            leftBorderMoveIsStarted = true;
            startResizeToLeft(event);

        } else if ((rightBorderTouchRect.contains(touchStartPoint.x, touchStartPoint.y)
                && !selectorMoveIsStarted)
                || rightBorderMoveIsStarted) {

            rightBorderMoveIsStarted = true;
            startResizeToRight(event);

        } else if (rangeSelectorPosition.contains((int) touchStartPoint.x, (int) touchStartPoint.y)
                || selectorMoveIsStarted) {

            selectorMoveIsStarted = true;
            startMove(event);
        }
    }

    private void startResizeToLeft(final MotionEvent event) {
        final float touchDiff = touchStartPoint.x - event.getX();
        final int newRectLeft = (int) (rangeSelectorScrollStartPosition.left - touchDiff);
        if (newRectLeft > 0) {
            if (newRectLeft < rangeSelectorScrollStartPosition.right - DEFAULT_SELECTED_RANGE_SIZE) {
                rangeSelectorPosition.left = newRectLeft;
            } else {
                rangeSelectorPosition.left = rangeSelectorScrollStartPosition.right - DEFAULT_SELECTED_RANGE_SIZE;
            }
        } else {
            rangeSelectorPosition.left = 0;
        }
    }

    private void startResizeToRight(final MotionEvent event) {
        final float touchDiff = touchStartPoint.x - event.getX();
        final int newRectRight = (int) (rangeSelectorScrollStartPosition.right - touchDiff);
        if (newRectRight < getWidth()) {
            if (newRectRight > rangeSelectorScrollStartPosition.left + DEFAULT_SELECTED_RANGE_SIZE) {
                rangeSelectorPosition.right = newRectRight;
            } else {
                rangeSelectorPosition.right = rangeSelectorScrollStartPosition.left + DEFAULT_SELECTED_RANGE_SIZE;
            }
        } else {
            rangeSelectorPosition.right = getWidth();
        }
    }

    private void startMove(final MotionEvent event) {
        final float touchDiff = touchStartPoint.x - event.getX();
        final int newRectLeft = (int) (rangeSelectorScrollStartPosition.left - touchDiff);
        final int rangeSelectorSize = rangeSelectorPosition.right - rangeSelectorPosition.left;

        rangeSelectorPosition.offsetTo(newRectLeft, 0);

        if (newRectLeft > 0 && newRectLeft + rangeSelectorSize < getWidth()) {
            rangeSelectorPosition.offsetTo(newRectLeft, 0);
        } else if (newRectLeft < 0) {
            rangeSelectorPosition.offsetTo(0, 0);
        } else if (newRectLeft + rangeSelectorSize > getWidth()) {
            rangeSelectorPosition.offsetTo(getWidth() - rangeSelectorSize, 0);
        }
    }

    void onRangeChanged() {
        if (onSelectorListener != null) {
            currentPreviewPosition = getSelectedGraphRange();
            onSelectorListener.onRangeChanged(currentPreviewPosition);
        }
    }

    public interface OnSelectorListener {

        void onRangeChanged(PreviewPosition previewPosition);
    }
}