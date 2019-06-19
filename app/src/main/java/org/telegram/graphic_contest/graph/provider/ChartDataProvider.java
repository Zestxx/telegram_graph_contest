package org.telegram.graphic_contest.graph.provider;


import org.telegram.graphic_contest.data.model.ChartData;

public interface ChartDataProvider {

    public ChartData getChartData();

    public void setChartData(ChartData data);

}
