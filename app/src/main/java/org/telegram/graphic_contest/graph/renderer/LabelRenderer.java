package org.telegram.graphic_contest.graph.renderer;

import org.telegram.graphic_contest.R;
import org.telegram.graphic_contest.data.model.AxisValue;
import org.telegram.graphic_contest.data.model.HighLightPointParams;
import org.telegram.graphic_contest.graph.computator.ChartComputator;
import org.telegram.graphic_contest.graph.util.ChartUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class LabelRenderer {

    private final Paint valuePaint;
    private final View labelView;
    private final float density;
    private final Context context;


    LabelRenderer(final Context context) {
        this.context = context;
        final Paint datePaint = new Paint();
        datePaint.setColor(ContextCompat.getColor(context, R.color.color_default_axis_line));
        final Paint labelFillPaint = new Paint();
        labelFillPaint.setStyle(Paint.Style.FILL);
        labelFillPaint.setColor(Color.WHITE);

        final Paint labelStrokePaint = new Paint();
        labelStrokePaint.setStyle(Paint.Style.STROKE);
        labelStrokePaint.setColor(Color.BLACK);
        labelStrokePaint.setStrokeWidth(2);
        this.density = context.getResources().getDisplayMetrics().density;

        labelView = LayoutInflater.from(context).inflate(R.layout.layout_lable, null);
        valuePaint = new Paint();
    }

    void drawLabel(final Canvas canvas, final List<HighLightPointParams> selectedPointsParam,
            final ChartComputator chartComputator) {

        int pointPositionX = 0;

        for (int i = 0; i < selectedPointsParam.size(); i++) {
            final HighLightPointParams pointParams = selectedPointsParam.get(i);
            final Rect textBounds = new Rect();
            final String valueString = String.valueOf(pointParams.getPointValue().getY());
            valuePaint.getTextBounds(valueString, 0, valueString.length(), textBounds);

            pointPositionX = (int) pointParams.getPositionX();
        }

        final Bitmap labelBitmap = getLabelBitmap(selectedPointsParam);

        final int top = ChartUtils.dp2px(density, 16);
        final int left;
        if (pointPositionX + labelBitmap.getWidth() < chartComputator.getChartWidth()) {
            left = pointPositionX + ChartUtils.dp2px(density, 8);
        } else {
            left = pointPositionX - labelBitmap.getWidth() - ChartUtils.dp2px(density, 8);
        }

        canvas.drawBitmap(labelBitmap, left, top, valuePaint);
        labelBitmap.recycle();
        final ViewGroup lineDataContainer = labelView.findViewById(R.id.label_data_container);
        lineDataContainer.removeAllViewsInLayout();
    }

    private Bitmap getLabelBitmap(final List<HighLightPointParams> selectedPointsParam) {

        final TextView labelDate = labelView.findViewById(R.id.label_date_text);
        final ViewGroup lineDataContainer = labelView.findViewById(R.id.label_data_container);

        for (int i = 0; i < selectedPointsParam.size(); i++) {
            final HighLightPointParams pointParams = selectedPointsParam.get(i);

            final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH);
            final AxisValue pointAxisValue = pointParams.getPointAxisValue();
            final CharSequence pointDate = dateFormat.format(new Date(pointAxisValue.getTime()));

            final String valueString = String.valueOf((int) pointParams.getPointValue().getY());
            String valueName = "";

            if (i == 0) {
                valueName = "Joined";
            } else if (i == 1) {
                valueName = "Left";
            } else {
                String.valueOf(i);
            }
            final View lineDataView = getLineDataView(pointParams.getPointColor(), valueString, valueName);
            lineDataContainer.addView(lineDataView);

            labelDate.setText(pointDate);
        }

        labelView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        labelView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        labelView.layout(0, 0, labelView.getMeasuredWidth(), labelView.getMeasuredHeight());
        return convertViewToBitmap(labelView);
    }

    private static Bitmap convertViewToBitmap(final View labelView) {
        final Bitmap bitmap = Bitmap.createBitmap(labelView.getMeasuredWidth(), labelView.getMeasuredHeight(),
                Bitmap.Config.ARGB_4444);

        final Canvas canvas = new Canvas(bitmap);
        labelView.layout(labelView.getLeft(), labelView.getTop(), labelView.getRight(), labelView.getBottom());
        labelView.draw(canvas);
        return bitmap;
    }

    private View getLineDataView(final int color, final String value, final String valueName) {
        final View lineDataView = LayoutInflater.from(context).inflate(R.layout.layout_line_data, null);
        final TextView lineValue = lineDataView.findViewById(R.id.label_date_value_text);
        final TextView lineValueName = lineDataView.findViewById(R.id.label_date_value_name_text);

        lineValue.setTextColor(color);
        lineValueName.setTextColor(color);

        lineValue.setText(value);
        lineValueName.setText(valueName);
        return lineDataView;
    }
}