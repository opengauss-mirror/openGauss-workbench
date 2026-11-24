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
package org.opengauss.admin.plugin.vo.modeling;

import lombok.Data;
import org.opengauss.admin.plugin.vo.modeling.component.Location;

import java.util.List;
import java.util.Objects;

/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Data
public class ScatterChartParamsBody {

    private String title;
    private String chartType;
    private String dataFlowId;
    private String operatorId;
    private Indicator indicator;
    private Location location;
    private Double zoom;
    private List<Double> center;
    private List<String> colorConfig;

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }
    public String getChartType() {
        return chartType;
    }

    public void setDataFlowId(String dataFlowId) {
        this.dataFlowId = dataFlowId;
    }
    public String getDataFlowId() {
        return dataFlowId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }
    public String getOperatorId() {
        return operatorId;
    }

    public void setIndicator(Indicator indicator) {
        this.indicator = indicator;
    }
    public Indicator getIndicator() {
        return indicator;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
    public Location getLocation() {
        return location;
    }

    public void setCenter(List<Double> center) {
        this.center = center;
    }
    public List<Double> getCenter() {
        return center.stream().limit(2).anyMatch(Objects::isNull) ? List.of(0d, 0d) : center;
    }

    public void setColorConfig(List<String> colorConfig) {
        this.colorConfig = colorConfig;
    }
    public List<String> getColorConfig() {
        return colorConfig;
    }
}
