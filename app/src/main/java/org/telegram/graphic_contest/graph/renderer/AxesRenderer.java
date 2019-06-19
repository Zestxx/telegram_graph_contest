package org.telegram.graphic_contest.graph.renderer;

import org.telegram.graphic_contest.data.model.Axis;
import org.telegram.graphic_contest.data.model.AxisValue;
import org.telegram.graphic_contest.data.model.Viewport;
import org.telegram.graphic_contest.graph.computator.ChartComputator;
import org.telegram.graphic_contest.graph.formatter.ValueFormatterHelper;
import org.telegram.graphic_contest.graph.util.AxisAutoValues;
import org.telegram.graphic_contest.graph.util.ChartUtils;
import org.telegram.graphic_contest.graph.util.FloatUtils;
import org.telegram.graphic_contest.graph.view.ChartView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.text.TextUtils;

public class AxesRenderer {

    private static final int DEFAULT_AXIS_MARGIN_DP = 2;
    private static final int HALF_RIGHT_ANGLE = 45;
    private static final int RIGHT_ANGLE = 90;

    private static final int TOP = 0;
    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private static final int BOTTOM = 3;


    private static final char[] labelWidthChars = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};

    private final ChartView mChartView;
    private ChartComputator computator;
    private final int axisMargin;
    private final float scaledDensity;
    private final Paint[] labelPaint = {new Paint(), new Paint(), new Paint(), new Paint()};
    private final Paint[] namePaint = {new Paint(), new Paint(), new Paint(), new Paint()};
    private final Paint[] linePaint = {new Paint(), new Paint(), new Paint(), new Paint()};
    private final float[] nameBaseline = new float[4];
    private final float[] labelBaseline = new float[4];
    private final float[] separationLine = new float[4];
    private final int[] labelWidth = new int[4];
    private final int[] labelTextAscent = new int[4];
    private final int[] labelTextDescent = new int[4];
    private final int[] labelDimensionForMargins = new int[4];
    private final int[] labelDimensionForSteps = new int[4];
    private final int[] tiltedLabelXTranslation = new int[4];
    private final int[] tiltedLabelYTranslation = new int[4];
    private final FontMetricsInt[] fontMetrics = {new FontMetricsInt(), new FontMetricsInt(),
            new FontMetricsInt(), new FontMetricsInt()};
    private final ValueFormatterHelper valueFormatterHelper = new ValueFormatterHelper();


    private final char[] labelBuffer = new char[64];


    private final int[] valuesToDrawNum = new int[4];


    private final float[][] rawValues = new float[4][0];


    private final float[][] autoValuesToDraw = new float[4][0];


    private final AxisValue[][] valuesToDraw = new AxisValue[4][0];


    private final float[][] linesDrawBuffer = new float[4][0];


    private final AxisAutoValues[] autoValuesBufferTab = {new AxisAutoValues(),
            new AxisAutoValues(), new AxisAutoValues(), new AxisAutoValues()};

    public AxesRenderer(final Context context, final ChartView chartView) {
        super();
        this.mChartView = chartView;
        computator = chartView.getChartComputator();
        final float density = context.getResources().getDisplayMetrics().density;
        scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        axisMargin = ChartUtils.dp2px(density, DEFAULT_AXIS_MARGIN_DP);
        for (int position = 0; position < 4; ++position) {
            labelPaint[position].setStyle(Paint.Style.FILL);
            labelPaint[position].setAntiAlias(true);
            namePaint[position].setStyle(Paint.Style.FILL);
            namePaint[position].setAntiAlias(true);
            linePaint[position].setStyle(Paint.Style.STROKE);
            linePaint[position].setAntiAlias(true);
        }
    }

    public void onChartSizeChanged() {
        onChartDataOrSizeChanged();
    }

    public void onChartDataChanged() {
        onChartDataOrSizeChanged();
    }

    private void onChartDataOrSizeChanged() {
        initAxis(mChartView.getChartData().getAxisXBottom(), BOTTOM);
        initAxis(mChartView.getChartData().getAxisYLeft(), LEFT);
    }

    public void resetRenderer() {
        this.computator = mChartView.getChartComputator();
    }


    private void initAxis(final Axis axis, final int position) {
        if (null == axis) {
            return;
        }
        initAxisAttributes(axis, position);
        initAxisMargin(axis, position);
        initAxisMeasurements(axis, position);
    }

    private void initAxisAttributes(final Axis axis, final int position) {
        initAxisPaints(axis, position);
        initAxisTextAlignment(axis, position);

        initAxisDimension(position);

    }

    private void initAxisPaints(final Axis axis, final int position) {

        labelPaint[position].setColor(axis.getTextColor());
        labelPaint[position].setTextSize(ChartUtils.sp2px(scaledDensity, axis.getTextSize()));
        labelPaint[position].getFontMetricsInt(fontMetrics[position]);
        namePaint[position].setColor(axis.getTextColor());
        namePaint[position].setTextSize(ChartUtils.sp2px(scaledDensity, axis.getTextSize()));
        linePaint[position].setColor(axis.getLineColor());
        labelTextAscent[position] = Math.abs(fontMetrics[position].ascent);
        labelTextDescent[position] = Math.abs(fontMetrics[position].descent);
        labelWidth[position] = (int) labelPaint[position].measureText(labelWidthChars, 0,
                axis.getMaxLabelChars());
    }

    private void initAxisTextAlignment(final Axis axis, final int position) {
        namePaint[position].setTextAlign(Align.CENTER);
        if (TOP == position || BOTTOM == position) {
            labelPaint[position].setTextAlign(Align.CENTER);
        } else if (LEFT == position) {
            labelPaint[position].setTextAlign(Align.RIGHT);
        } else if (RIGHT == position) {
            labelPaint[position].setTextAlign(Align.LEFT);
        }
    }

    private void initAxisDimension(final int position) {
        if (LEFT == position || RIGHT == position) {
            labelDimensionForMargins[position] = labelWidth[position];
            labelDimensionForSteps[position] = labelTextAscent[position];
        } else if (TOP == position || BOTTOM == position) {
            labelDimensionForMargins[position] = labelTextAscent[position] +
                    labelTextDescent[position];
            labelDimensionForSteps[position] = labelWidth[position];
        }
    }

    private void initAxisMargin(final Axis axis, final int position) {
        int margin = 0;
        if ((axis.isAutoGenerated() || !axis.getValues().isEmpty())) {
            margin += axisMargin + labelDimensionForMargins[position];
        }
        margin += getAxisNameMargin(axis, position);
        insetContentRectWithAxesMargins(margin, position);
    }

    private int getAxisNameMargin(final Axis axis, final int position) {
        int margin = 0;
        if (!TextUtils.isEmpty(axis.getName())) {
            margin += labelTextAscent[position];
            margin += labelTextDescent[position];
            margin += axisMargin;
        }
        return margin;
    }

    private void insetContentRectWithAxesMargins(final int axisMargin, final int position) {
        if (LEFT == position) {
            mChartView.getChartComputator().insetContentRect(axisMargin, 0, 0, 0);
        } else if (RIGHT == position) {
            mChartView.getChartComputator().insetContentRect(0, 0, axisMargin, 0);
        } else if (TOP == position) {
            mChartView.getChartComputator().insetContentRect(0, axisMargin, 0, 0);
        } else if (BOTTOM == position) {
            mChartView.getChartComputator().insetContentRect(0, 0, 0, axisMargin);
        }
    }

    private void initAxisMeasurements(final Axis axis, final int position) {
        if (LEFT == position) {

            labelBaseline[position] = computator.getContentRectMinusAxesMargins().left - axisMargin;
            nameBaseline[position] = labelBaseline[position] - axisMargin
                    - labelTextDescent[position] - labelDimensionForMargins[position];

            separationLine[position] = computator.getContentRectMinusAllMargins().left;
        } else if (RIGHT == position) {

            labelBaseline[position] = computator.getContentRectMinusAxesMargins().right + axisMargin;

            nameBaseline[position] = labelBaseline[position] + axisMargin + labelTextAscent[position]
                    + labelDimensionForMargins[position];
            separationLine[position] = computator.getContentRectMinusAllMargins().right;
        } else if (BOTTOM == position) {

            labelBaseline[position] = computator.getContentRectMinusAxesMargins().bottom + axisMargin
                    + labelTextAscent[position];

            nameBaseline[position] = labelBaseline[position] + axisMargin + labelDimensionForMargins[position];

            separationLine[position] = computator.getContentRectMinusAllMargins().bottom;
        } else if (TOP == position) {

            labelBaseline[position] = computator.getContentRectMinusAxesMargins().top - axisMargin
                    - labelTextDescent[position];

            nameBaseline[position] = labelBaseline[position] - axisMargin - labelDimensionForMargins[position];
            separationLine[position] = computator.getContentRectMinusAllMargins().top;
        } else {
            throw new IllegalArgumentException("Invalid axis position: " + position);
        }
    }

    public void drawInBackground(final Canvas canvas) {
        final Axis axis = mChartView.getChartData().getAxisYLeft();
        if (null != axis) {
            prepareAxisToDraw(axis, LEFT);
            drawAxisLines(canvas, axis, LEFT);
        }

        final Axis axisXBottom = mChartView.getChartData().getAxisXBottom();
        if (null != axisXBottom) {
            prepareAxisToDraw(axisXBottom, BOTTOM);
            drawAxisLines(canvas, axisXBottom, BOTTOM);
        }
    }

    private void prepareAxisToDraw(final Axis axis, final int position) {
        if (axis.isAutoGenerated()) {
            prepareAutoGeneratedAxis(axis, position);
        } else {
            prepareCustomAxis(axis, position);
        }
    }


    public void drawInForeground(final Canvas canvas) {
        final Axis axis = mChartView.getChartData().getAxisYLeft();
        if (null != axis) {
            drawAxisLabelsAndName(canvas, axis, LEFT);
        }
        final Axis axisXBottom = mChartView.getChartData().getAxisXBottom();
        if (null != axisXBottom) {
            drawAxisLabelsAndName(canvas, axisXBottom, BOTTOM);
        }

    }

    private void prepareCustomAxis(final Axis axis, final int position) {
        final Viewport maxViewport = computator.getMaximumViewport();
        final Viewport visibleViewport = computator.getVisibleViewport();
        final Rect contentRect = computator.getContentRectMinusAllMargins();
        final boolean isAxisVertical = isAxisVertical(position);
        float scale = 1;
        if (isAxisVertical) {
            if (maxViewport.height() > 0 && visibleViewport.height() > 0) {
                scale = contentRect.height() * (maxViewport.height() / visibleViewport.height());
            }
        } else {
            if (maxViewport.width() > 0 && visibleViewport.width() > 0) {
                scale = contentRect.width() * (maxViewport.width() / visibleViewport.width());
            }
        }
        if (scale == 0) {
            scale = 1;
        }
        final int module = (int) Math.max(1,
                Math.ceil((axis.getValues().size() * labelDimensionForSteps[position] * 2) / scale));
        //Reinitialize tab to hold lines coordinates.
        if (axis.hasLines() && (linesDrawBuffer[position].length < axis.getValues().size() * 4)) {
            linesDrawBuffer[position] = new float[axis.getValues().size() * 4];
        }
        //Reinitialize tabs to hold all raw values to draw.
        if (rawValues[position].length < axis.getValues().size()) {
            rawValues[position] = new float[axis.getValues().size()];
        }
        //Reinitialize tabs to hold all raw values to draw.
        if (valuesToDraw[position].length < axis.getValues().size()) {
            valuesToDraw[position] = new AxisValue[axis.getValues().size()];
        }

        int valueIndex = 0;
        int valueToDrawIndex = 0;

        for (final AxisValue axisValue : axis.getValues()) {
            final float value = axisValue.getValue();
            if (0 == valueIndex % module) {
                final float rawValue;
                if (isAxisVertical) {
                    rawValue = computator.computeRawY(value);
                } else {
                    rawValue = computator.computeRawX(value);
                }
                rawValues[position][valueToDrawIndex] = rawValue;
                valuesToDraw[position][valueToDrawIndex] = axisValue;
                ++valueToDrawIndex;
            }
            ++valueIndex;
        }
        valuesToDrawNum[position] = valueToDrawIndex;
    }

    private void prepareAutoGeneratedAxis(final Axis axis, final int position) {
        final Viewport visibleViewport = computator.getVisibleViewport();
        final Rect contentRect = computator.getContentRectMinusAllMargins();
        final boolean isAxisVertical = isAxisVertical(position);
        final float start;
        final float stop;
        final int contentRectDimension;
        if (isAxisVertical) {
            start = 0;
            stop = visibleViewport.top;
            contentRectDimension = contentRect.height();
        } else {
            start = visibleViewport.left;
            stop = visibleViewport.right;
            contentRectDimension = contentRect.width();
        }
        final AxisAutoValues axisAutoValue = FloatUtils
                .computeAutoGeneratedAxisValues(start, stop, Math.abs(contentRectDimension) /
                        labelDimensionForSteps[position] / 2, autoValuesBufferTab[position]);
        //Reinitialize tab to hold lines coordinates.
        if (axis.hasLines()
                && (linesDrawBuffer[position].length < axisAutoValue.valuesNumber * 4)) {
            linesDrawBuffer[position] = new float[axisAutoValue.valuesNumber * 4];
        }
        //Reinitialize tabs to hold all raw and auto values.
        if (rawValues[position].length < axisAutoValue.valuesNumber) {
            rawValues[position] = new float[axisAutoValue.valuesNumber];
        }
        if (autoValuesToDraw[position].length < axisAutoValue.valuesNumber) {
            autoValuesToDraw[position] = new float[axisAutoValue.valuesNumber];
        }

        int valueToDrawIndex = 0;
        for (int i = 0; i < axisAutoValue.valuesNumber; ++i) {
            final float rawValue;
            if (isAxisVertical) {
                rawValue = computator.computeRawY(autoValuesBufferTab[position].values[i]);
            } else {
                rawValue = computator.computeRawX(autoValuesBufferTab[position].values[i]);
            }
            rawValues[position][valueToDrawIndex] = rawValue;
            autoValuesToDraw[position][valueToDrawIndex] = autoValuesBufferTab[position].values[i];
            ++valueToDrawIndex;
        }
        valuesToDrawNum[position] = valueToDrawIndex;
    }

    private void drawAxisLines(final Canvas canvas, final Axis axis, final int position) {
        final Rect contentRectMargins = computator.getContentRectMinusAxesMargins();

        float lineY1 = 0;
        float lineX1 = 0;
        float lineX2 = 0;
        float lineY2 = 0;

        final boolean isAxisVertical = isAxisVertical(position);
        if (LEFT == position || RIGHT == position) {
            lineX1 = contentRectMargins.left;
            lineX2 = contentRectMargins.right;
        } else if (TOP == position || BOTTOM == position) {
            lineY1 = contentRectMargins.top;
            lineY2 = contentRectMargins.bottom;
        }

        if (axis.hasLines()) {
            int valueToDrawIndex = 0;
            for (; valueToDrawIndex < valuesToDrawNum[position]; ++valueToDrawIndex) {
                if (isAxisVertical) {
                    lineY1 = lineY2 = rawValues[position][valueToDrawIndex];
                } else {
                    lineX1 = lineX2 = rawValues[position][valueToDrawIndex];
                }
                linesDrawBuffer[position][valueToDrawIndex * 4] = lineX1;
                linesDrawBuffer[position][valueToDrawIndex * 4 + 1] = lineY1;
                linesDrawBuffer[position][valueToDrawIndex * 4 + 2] = lineX2;
                linesDrawBuffer[position][valueToDrawIndex * 4 + 3] = lineY2;
            }
            canvas.drawLines(linesDrawBuffer[position], 0, valueToDrawIndex * 4, linePaint[position]);
        }
    }

    private void drawAxisLabelsAndName(final Canvas canvas, final Axis axis, final int position) {
        float labelY = 0;
        float labelX = 0;

        final boolean isAxisVertical = isAxisVertical(position);
        if (LEFT == position || RIGHT == position) {
            labelX = labelBaseline[position];
        } else if (TOP == position || BOTTOM == position) {
            labelY = labelBaseline[position];
        }

        for (int valueToDrawIndex = 0; valueToDrawIndex < valuesToDrawNum[position]; ++valueToDrawIndex) {
            final int charsNumber;
            if (axis.isAutoGenerated()) {
                final float value = autoValuesToDraw[position][valueToDrawIndex];
                charsNumber = formatValueForAutoGeneratedAxis(labelBuffer, value,
                        autoValuesBufferTab[position].decimals);
            } else {
                final AxisValue axisValue = valuesToDraw[position][valueToDrawIndex];
                charsNumber = formatValueForManualAxis(labelBuffer, axisValue);
            }

            if (isAxisVertical) {
                labelY = rawValues[position][valueToDrawIndex];
            } else {
                labelX = rawValues[position][valueToDrawIndex];
            }
            canvas.drawText(labelBuffer, labelBuffer.length - charsNumber, charsNumber, labelX, labelY,
                    labelPaint[position]);
        }

        // Drawing axis name
        final Rect contentRectMargins = computator.getContentRectMinusAxesMargins();
        if (!TextUtils.isEmpty(axis.getName())) {
            if (isAxisVertical) {
                canvas.save();
                canvas.rotate(-RIGHT_ANGLE, contentRectMargins.centerY(), contentRectMargins.centerY());
                canvas.drawText(axis.getName(), contentRectMargins.centerY(), nameBaseline[position],
                        namePaint[position]);
                canvas.restore();
            } else {
                canvas.drawText(axis.getName(), contentRectMargins.centerX(), nameBaseline[position],
                        namePaint[position]);
            }
        }
    }

    private static boolean isAxisVertical(final int position) {
        if (LEFT == position || RIGHT == position) {
            return true;
        } else if (TOP == position || BOTTOM == position) {
            return false;
        } else {
            throw new IllegalArgumentException("Invalid axis position " + position);
        }
    }

    private int formatValueForManualAxis(final char[] formattedValue, final AxisValue axisValue) {
        return valueFormatterHelper.formatFloatValueWithPrependedAndAppendedText(formattedValue,
                axisValue.getValue(), axisValue.getLabelAsChars());
    }

    private int formatValueForAutoGeneratedAxis(final char[] formattedValue, final float value,
            final int autoDecimalDigits) {
        return valueFormatterHelper.formatFloatValueWithPrependedAndAppendedText(formattedValue,
                value, autoDecimalDigits);
    }

}