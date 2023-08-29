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
