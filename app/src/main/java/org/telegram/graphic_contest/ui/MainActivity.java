package org.telegram.graphic_contest.ui;

import org.telegram.graphic_contest.R;
import org.telegram.graphic_contest.data.PreviewPosition;
import org.telegram.graphic_contest.data.ThemeData;
import org.telegram.graphic_contest.data.gson.GsonUtil;
import org.telegram.graphic_contest.data.model.Axis;
import org.telegram.graphic_contest.data.model.AxisValue;
import org.telegram.graphic_contest.data.model.ChartData;
import org.telegram.graphic_contest.data.model.Column;
import org.telegram.graphic_contest.data.model.Graph;
import org.telegram.graphic_contest.data.model.Line;
import org.telegram.graphic_contest.data.model.PointValue;
import org.telegram.graphic_contest.graph.gesture.ZoomType;
import org.telegram.graphic_contest.graph.view.ChartRangeSelector;
import org.telegram.graphic_contest.graph.view.ChartView;
import org.telegram.graphic_contest.util.AssetsFileReader;
import org.telegram.graphic_contest.util.ThemeUtil;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ChartView previewGraphView;
    private ChartView scrollableGraphView;
    private ChartRangeSelector rangeSelector;
    private CheckBox joinedCheckBox;
    private CheckBox leftCheckBox;
    private ImageButton changeThemeButton;
    private ImageButton chartMenuButton;
    private FrameLayout customToolbar;
    private ConstraintLayout mainContainer;
    private TextView joinedTextView;
    private TextView leftTextView;
    private View selectorContainer;
    private final Map<Integer, Line> linesAndCheckerMap = new HashMap<>();
    private final ThemeUtil colorSchemeUtil = new ThemeUtil();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setView();
        initData(0);
    }

    private void initViews() {
        previewGraphView = findViewById(R.id.total_graph_view);
        scrollableGraphView = findViewById(R.id.preview_graph_view);
        rangeSelector = findViewById(R.id.range_selector);
        joinedCheckBox = findViewById(R.id.joined_check_box);
        leftCheckBox = findViewById(R.id.left_check_box);
        changeThemeButton = findViewById(R.id.theme_change_button);
        chartMenuButton = findViewById(R.id.chart_select_button);
        customToolbar = findViewById(R.id.toolbar);
        mainContainer = findViewById(R.id.main_container);
        joinedTextView = findViewById(R.id.joined_text_view);
        leftTextView = findViewById(R.id.left_text_view);
        selectorContainer = findViewById(R.id.selector_container);
    }

    private void setView() {
        joinedCheckBox.setChecked(true);
        leftCheckBox.setChecked(true);

        final CompoundButton.OnCheckedChangeListener checkBoxListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {

                if (joinedCheckBox.isChecked()) {
                    scrollableGraphView.getChartData().setHideJoined(false);
                    previewGraphView.getChartData().setHideJoined(false);
                } else {
                    scrollableGraphView.getChartData().setHideJoined(true);
                    previewGraphView.getChartData().setHideJoined(true);
                }

                if (leftCheckBox.isChecked()) {
                    scrollableGraphView.getChartData().setHideLeft(false);
                    previewGraphView.getChartData().setHideLeft(false);
                } else {
                    scrollableGraphView.getChartData().setHideLeft(true);
                    previewGraphView.getChartData().setHideLeft(true);
                }

                scrollableGraphView.setPositionByPreview(rangeSelector.getCurrentPreviewPosition());
                previewGraphView.updateWithAnimation();
            }
        };
        joinedCheckBox.setOnCheckedChangeListener(checkBoxListener);
        leftCheckBox.setOnCheckedChangeListener(checkBoxListener);

        changeThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final ThemeData newTheme = colorSchemeUtil.switchTheme();
                setColorScheme(newTheme);
            }
        });

        chartMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final PopupMenu popup = new PopupMenu(MainActivity.this, v);
                popup.getMenuInflater().inflate(R.menu.chart_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        selectorContainer.setVisibility(View.VISIBLE);
                        switch (item.getItemId()) {
                            case R.id.chart_1:
                                initData(0);
                                break;

                            case R.id.chart_2:
                                initData(1);
                                break;

                            case R.id.chart_3:
                                initData(2);
                                break;

                            case R.id.chart_4:
                                initData(3);
                                break;

                            case R.id.chart_5:
                                initData(4);
                                selectorContainer.setVisibility(View.GONE);
                                break;
                        }
                        scrollableGraphView.setPositionByPreview(rangeSelector.getCurrentPreviewPosition());
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    private void initData(final int chart) {
        final Graph[] graphs = getGraphs();
        final Graph graph = graphs[chart];

        bindDataToGraph(graph);
    }

    private Graph[] getGraphs() {
        final String json = getDataJson();
        return GsonUtil.getGson().fromJson(json, Graph[].class);
    }

    private String getDataJson() {
        return new AssetsFileReader(this)
                .getString("chart_data.json");
    }

    private void bindDataToGraph(final Graph graph) {
        final List<Line> lines = new ArrayList<>();
        final List<AxisValue> axisXValues = new ArrayList<>();

        for (final Column column : graph.getColumns()) {
            if (column.getName().equals("x")) {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
                for (int i = 0; i < column.getValues().size(); i++) {
                    final long value = column.getValues().get(i);
                    final String formattedDate = dateFormat.format(new Date(value));
                    final AxisValue axisValue = new AxisValue(i, value);
                    axisValue.setLabel(formattedDate);
                    axisXValues.add(axisValue);
                }
            } else {
                final String lineColor = graph.getColors().get(column.getName());
                final int hexLineColor = Color.parseColor(lineColor);
                final Line line = new Line();

                if (column.getName().equals("y0")) {
                    line.setValues(getNewGraphElements(column));
                    linesAndCheckerMap.put(joinedCheckBox.getId(), line);
                    setCheckBoxColor(joinedCheckBox, hexLineColor);
                }

                if (column.getName().equals("y1")) {
                    line.setValues(getNewGraphElements(column));
                    linesAndCheckerMap.put(leftCheckBox.getId(), line);
                    setCheckBoxColor(leftCheckBox, hexLineColor);
                }

                if (column.getName().equals("y2")) {
                    line.setValues(getNewGraphElements(column));
                    linesAndCheckerMap.put(joinedCheckBox.getId(), line);
                    setCheckBoxColor(joinedCheckBox, hexLineColor);
                }

                if (column.getName().equals("y3")) {
                    line.setValues(getNewGraphElements(column));
                    linesAndCheckerMap.put(leftCheckBox.getId(), line);
                    setCheckBoxColor(leftCheckBox, hexLineColor);
                }
                line.setColor(hexLineColor)
                        .setStrokeWidth(2);

                lines.add(line);
            }
        }
        final ChartData data = new ChartData(lines);

        final Axis axisX = new Axis();
        final Axis axisY = new Axis().setHasLines(true);

        axisX.setValues(axisXValues);
        axisX.setMaxLabelChars(5);

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        scrollableGraphView.setChartData(data);
        scrollableGraphView.setZoomType(ZoomType.HORIZONTAL);
        scrollableGraphView.setGestureIsEnable(false);

        final List<Line> previewLines = new ArrayList<>();
        for (final Line line : lines) {
            final Line previewLine = new Line(line);
            previewLines.add(previewLine.setStrokeWidth(1));
        }
        final ChartData previewData = new ChartData(previewLines);

        previewGraphView.setChartData(previewData);
        previewGraphView.setInteractive(false);
        rangeSelector.setOnSelectorListener(new ChartRangeSelector.OnSelectorListener() {
            @Override
            public void onRangeChanged(final PreviewPosition previewPosition) {
                scrollableGraphView.setPositionByPreview(previewPosition);
            }
        });
    }

    private static void setCheckBoxColor(final CheckBox checkBox, final int color) {
        CompoundButtonCompat.setButtonTintList(checkBox,
                ColorStateList.valueOf(color));
    }

    private static List<PointValue> getNewGraphElements(final Column column) {
        final List<PointValue> graphPoints = new ArrayList<>();
        for (int i = 0; i < column.getValues().size(); i++) {
            final long value = column.getValues().get(i);
            graphPoints.add(new PointValue(i, value));
        }
        graphPoints.add(0, new PointValue(0, 0));
        return graphPoints;
    }

    private void setColorScheme(final ThemeData newTheme) {
        customToolbar.setBackgroundColor(ContextCompat.getColor(this, newTheme.getToolbarColor()));
        mainContainer.setBackgroundColor(ContextCompat.getColor(this, newTheme.getBackgroundColor()));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, newTheme.getStatusBarColor()));

        joinedTextView.setTextColor(ContextCompat.getColor(this, newTheme.getTextColor()));
        leftTextView.setTextColor(ContextCompat.getColor(this, newTheme.getTextColor()));
        rangeSelector.setOverlayColor(ContextCompat.getColor(this, newTheme.getRangeSelectorOverlayColor()));
        scrollableGraphView.getChartRenderer()
                .setPointInnerColor(ContextCompat.getColor(this, newTheme.getBackgroundColor()));
        scrollableGraphView.invalidate();
    }
}