/**
 Copyright  (c) 2020 Huawei Technologies Co.,Ltd.
 Copyright  (c) 2021 openGauss Contributors

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
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
