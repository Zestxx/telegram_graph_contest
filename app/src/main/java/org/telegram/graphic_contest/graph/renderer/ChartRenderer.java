package org.telegram.graphic_contest.graph.renderer;

import org.telegram.graphic_contest.R;
import org.telegram.graphic_contest.data.model.AxisValue;
import org.telegram.graphic_contest.data.model.ChartData;
import org.telegram.graphic_contest.data.model.HighLightPointParams;
import org.telegram.graphic_contest.data.model.Line;
import org.telegram.graphic_contest.data.model.PointValue;
import org.telegram.graphic_contest.data.model.SelectedValue;
import org.telegram.graphic_contest.data.model.Viewport;
import org.telegram.graphic_contest.graph.computator.ChartComputator;
import org.telegram.graphic_contest.graph.provider.ChartDataProvider;
import org.telegram.graphic_contest.graph.util.ChartUtils;
import org.telegram.graphic_contest.graph.view.ChartView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;


public class ChartRenderer {

    private static final int DEFAULT_LINE_STROKE_WIDTH_DP = 4;
    private static final int DEFAULT_TOUCH_TOLERANCE_MARGIN_DP = 4;
    private static final int DEFAULT_AREA_TRANSPARENCY = 64;


    private ChartDataProvider dataProvider;
    private float baseValue;

    private final int touchToleranceMargin;
    private final Path path = new Path();
    private final Paint linePaint = new Paint();
    private final Paint pointPaint = new Paint();
    private final Paint pointInnerPaint = new Paint();
    private final Paint highLightLinePaint = new Paint();

    private Bitmap softwareBitmap;
    private final Canvas softwareCanvas = new Canvas();
    private final Viewport tempMaximumViewport;
    private final LabelRenderer labelRenderer;


    private final ChartView chartView;
    private ChartComputator computator;

    private final float density;
    private final SelectedValue selectedValue = new SelectedValue();

    public ChartRenderer(final Context context, final ChartView chartView, final ChartDataProvider dataProvider) {
        this.chartView = chartView;
        this.dataProvider = dataProvider;
        this.density = context.getResources().getDisplayMetrics().density;
        this.computator = chartView.getChartComputator();

        this.dataProvider = dataProvider;

        touchToleranceMargin = ChartUtils.dp2px(density, DEFAULT_TOUCH_TOLERANCE_MARGIN_DP);

        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(ChartUtils.dp2px(density, DEFAULT_LINE_STROKE_WIDTH_DP));

        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);

        highLightLinePaint.setColor(ContextCompat.getColor(context, R.color.color_default_axis_line));
        highLightLinePaint.setStrokeWidth(3);

        pointInnerPaint.setColor(Color.WHITE);
        pointInnerPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        labelRenderer = new LabelRenderer(context);
        tempMaximumViewport = new Viewport();
    }


    public void resetRenderer() {
        this.computator = chartView.getChartComputator();
    }


    public void onChartDataChanged() {
        selectedValue.clear();

        final int internalMargin = calculateContentRectInternalMargin();
        computator.insetContentRectByInternalMargins(internalMargin, internalMargin,
                internalMargin, internalMargin);
        baseValue = dataProvider.getChartData().getBaseValue();

        onChartViewportChanged();
    }

    public void onChartSizeChanged() {
        final int internalMargin = calculateContentRectInternalMargin();
        computator.insetContentRectByInternalMargins(internalMargin, internalMargin,
                internalMargin, internalMargin);
        if (computator.getChartWidth() > 0 && computator.getChartHeight() > 0) {
            softwareBitmap = Bitmap.createBitmap(computator.getChartWidth(), computator.getChartHeight(),
                    Bitmap.Config.ARGB_4444);
            softwareCanvas.setBitmap(softwareBitmap);
        }
    }

    public void draw(final Canvas canvas) {
        final ChartData data = dataProvider.getChartData();

        final Canvas drawCanvas;

        if (null != softwareBitmap) {
            drawCanvas = softwareCanvas;
            drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        } else {
            drawCanvas = canvas;
        }

        for (final Line line : data.getLines()) {
            if (line.hasLines()) {
                drawPath(drawCanvas, line);
            }
        }

        if (null != softwareBitmap) {
            canvas.drawBitmap(softwareBitmap, 0, 0, null);
        }
    }

    public void drawTouched(final Canvas canvas) {
        if (isTouched()) {
            highlightPoints(canvas);
        }
    }

    private static boolean checkIfShouldDrawPoints(final Line line) {
        return line.hasPoints() || line.getValues().size() == 1;
    }

    public boolean checkTouch(final float touchX, final float touchY) {
        selectedValue.clear();
        final ChartData data = dataProvider.getChartData();
        int lineIndex = 0;
        for (final Line line : data.getLines()) {
            if (checkIfShouldDrawPoints(line)) {
                final int pointRadius = ChartUtils.dp2px(density, 10);
                int valueIndex = 0;
                for (final PointValue pointValue : line.getValues()) {
                    final float rawValueX = computator.computeRawX(pointValue.getX());
                    final float rawValueY = computator.computeRawY(pointValue.getY());
                    if (isInArea(rawValueX, rawValueY, touchX, touchY, pointRadius + touchToleranceMargin)) {
                        selectedValue.set(lineIndex, valueIndex);
                    }
                    ++valueIndex;
                }
            }
            ++lineIndex;
        }
        return isTouched();
    }

    private void calculateMaxViewport() {
        tempMaximumViewport.set(Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MAX_VALUE);
        final ChartData data = dataProvider.getChartData();

        for (final Line line : data.getLines()) {

            for (final PointValue pointValue : line.getValues()) {
                if (pointValue.getX() < tempMaximumViewport.left) {
                    tempMaximumViewport.left = pointValue.getX();
                }
                if (pointValue.getX() > tempMaximumViewport.right) {
                    tempMaximumViewport.right = pointValue.getX();
                }
                if (pointValue.getY() < tempMaximumViewport.bottom) {
                    tempMaximumViewport.bottom = pointValue.getY();
                }
                if (pointValue.getY() > tempMaximumViewport.top) {
                    tempMaximumViewport.top = pointValue.getY();
                }

            }
        }
    }

    private int calculateContentRectInternalMargin() {
        int contentAreaMargin = 0;
        final ChartData data = dataProvider.getChartData();
        for (final Line line : data.getLines()) {
            if (checkIfShouldDrawPoints(line)) {
                final int margin = line.getPointRadius() + DEFAULT_TOUCH_TOLERANCE_MARGIN_DP;
                if (margin > contentAreaMargin) {
                    contentAreaMargin = margin;
                }
            }
        }
        return ChartUtils.dp2px(density, contentAreaMargin);
    }


    private void drawPath(final Canvas canvas, final Line line) {
        prepareLinePaint(line);

        int valueIndex = 0;
        for (final PointValue pointValue : line.getValues()) {
            final float rawX = computator.computeRawX(pointValue.getX());
            final float rawY = computator.computeRawY(pointValue.getY());

            if (valueIndex == 0) {
                path.moveTo(rawX, rawY);
            } else {
                path.lineTo(rawX, rawY);
            }

            ++valueIndex;

        }

        canvas.drawPath(path, linePaint);
        path.rewind();
    }

    private void prepareLinePaint(final Line line) {
        linePaint.setStrokeWidth(ChartUtils.dp2px(density, line.getStrokeWidth()));
        linePaint.setColor(line.getColor());
        linePaint.setPathEffect(line.getPathEffect());
        linePaint.setShader(null);
    }

    private void highlightPoints(final Canvas canvas) {
        final List<Line> lines = dataProvider.getChartData().getLines();
        final List<Integer> selectedValuePositions = new ArrayList<>(lines.size());

        final List<HighLightPointParams> selectedPointsParam = new ArrayList<>(lines.size());
        for (final Line line : lines) {

            final PointValue pointValue = line.getValues().get(selectedValue.getSecondIndex());
            if (dataProvider.getChartData().getAxisXBottom().getValues().size() > selectedValue.getSecondIndex()) {
                final AxisValue pointAxisValue = dataProvider.getChartData().getAxisXBottom().getValues()
                        .get(selectedValue.getSecondIndex());

                final float rawX = computator.computeRawX(pointValue.getX());
                final float rawY = computator.computeRawY(pointValue.getY());

                final int pointRadius = ChartUtils.dp2px(density, line.getPointRadius());

                final HighLightPointParams pointParams = new HighLightPointParams(rawX, rawY, line.getDarkenColor(),
                        pointRadius, pointValue, pointAxisValue);

                selectedPointsParam.add(pointParams);
                selectedValuePositions.add((int) rawY);
            }
        }

        if (!selectedValuePositions.isEmpty()) {
            final int highLightVerticalLineStart = 0;
            final int highLightVerticalLineEnd = (int) computator.computeRawY(0);
            final int highLightVerticalLinePositionX = (int) selectedPointsParam.get(0).getPositionX();

            canvas.drawLine(
                    highLightVerticalLinePositionX,
                    highLightVerticalLineStart,
                    highLightVerticalLinePositionX,
                    highLightVerticalLineEnd,
                    highLightLinePaint);

            for (int i = 0; i < selectedPointsParam.size(); i++) {
                final HighLightPointParams pointParams = selectedPointsParam.get(i);

                pointPaint.setColor(pointParams.getPointColor());

                canvas.drawCircle(pointParams.getPositionX(), pointParams.getPositionY(),
                        pointParams.getPointRadius() + touchToleranceMargin, pointPaint);

                canvas.drawCircle(pointParams.getPositionX(), pointParams.getPositionY(),
                        pointParams.getPointRadius() + 5, pointInnerPaint);
            }

            labelRenderer.drawLabel(canvas, selectedPointsParam, computator);
        }
    }

    private static boolean isInArea(final float x, final float y, final float touchX, final float touchY,
            final float radius) {
        final float diffX = touchX - x;
        final float diffY = touchY - y;
        return Math.pow(diffX, 2) + Math.pow(diffY, 2) <= 2 * Math.pow(radius, 2);
    }


    public boolean isTouched() {
        return selectedValue.isSet();
    }


    public Viewport getMaximumViewport() {
        return computator.getMaximumViewport();
    }

    public Viewport getCurrentViewport() {
        return computator.getCurrentViewport();
    }

    public void setCurrentViewport(final Viewport viewport) {
        if (null != viewport) {
            computator.setCurrentViewport(viewport);
        }
    }

    public SelectedValue getSelectedValue() {
        return selectedValue;
    }

    public void setPointInnerColor(final int color) {
        pointInnerPaint.setColor(color);
    }

    private void onChartViewportChanged() {
        calculateMaxViewport();
        computator.setMaxViewport(tempMaximumViewport);
        computator.setCurrentViewport(computator.getMaximumViewport());
    }
}