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
public class HeatmapChartBody {

    private Title title;
    private Tooltip tooltip;
    private List<HeatmapSeries> series;
    private XAxis xAxis;
    private XAxis yAxis;
    private VisualMap visualMap;

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

    public void setXAxis(XAxis xAxis) {
        this.xAxis = xAxis;
    }
    public XAxis getXAxis() {
        return xAxis;
    }

    public void setYAxis(XAxis yAxis) {
        this.yAxis = yAxis;
    }
    public XAxis getYAxis() {
        return yAxis;
    }

    public void setVisualMap(VisualMap visualMap) {
        this.visualMap = visualMap;
    }
    public VisualMap getVisualMap() {
        return visualMap;
    }

    public void setSeries(List<HeatmapSeries> series) {
        this.series = series;
    }
    public List<HeatmapSeries> getSeries() {
        return series;
    }


}
