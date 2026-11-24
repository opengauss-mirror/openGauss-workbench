/*
 * Copyright (c) 2020 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package org.opengauss.admin.plugin.domain.model.modeling.echart;

import org.opengauss.admin.plugin.vo.modeling.component.*;

import java.util.List;

/**
 *
 * @author LZW
 */

public class BarChartBody {

    private Title title;
    private Tooltip tooltip;
    private Legend legend;
    private Grid grid;
    private List<DataZoomItem> dataZoom;
    private XAxis xAxis;
    private YAxis yAxis;
    private List<Series> series;
    public void setTitle(Title title) {
        this.title = title;
    }
    public Title getTitle() {
        return title;
    }

    public void setTooltip(Tooltip tooltip) {
        this.tooltip = tooltip;
    }
    public Tooltip getTooltip() {
        return tooltip;
    }

    public void setLegend(Legend legend) {
        this.legend = legend;
    }
    public Legend getLegend() {
        return legend;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }
    public Grid getGrid() {
        return grid;
    }

    public void setXAxis(XAxis xAxis) {
        this.xAxis = xAxis;
    }
    public XAxis getXAxis() {
        return xAxis;
    }

    public void setYAxis(YAxis yAxis) {
        this.yAxis = yAxis;
    }
    public YAxis getYAxis() {
        return yAxis;
    }

    public void setSeries(List<Series> series) {
        this.series = series;
    }
    public List<Series> getSeries() {
        return series;
    }

    public List<DataZoomItem> getDataZoom() {
        return dataZoom;
    }

    public void setDataZoom(List<DataZoomItem> dataZoom) {
        this.dataZoom = dataZoom;
    }

}
