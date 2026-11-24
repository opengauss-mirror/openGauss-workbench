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
import org.opengauss.admin.plugin.vo.modeling.component.Legend;
import org.opengauss.admin.plugin.vo.modeling.component.PieSeries;
import org.opengauss.admin.plugin.vo.modeling.component.Title;
import org.opengauss.admin.plugin.vo.modeling.component.Tooltip;

import java.util.List;
/**
 *
 * @author LZW
 */
public class PieChartBody {

    private Title title;
    private Tooltip tooltip;
    private Legend legend;
    private List<PieSeries> series;

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

    public void setSeries(List<PieSeries> series) {
         this.series = series;
     }
     public List<PieSeries> getSeries() {
         return series;
     }

    public void setTitle(Title title) {
        this.title = title;
    }
    public Title getTitle() {
        return title;
    }
}
