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

import lombok.Data;
import org.opengauss.admin.plugin.vo.modeling.component.GraphSeries;
import org.opengauss.admin.plugin.vo.modeling.component.Title;
import org.opengauss.admin.plugin.vo.modeling.component.Tooltip;

import java.util.List;
/**
 *
 * @author LZW
 */
@Data
public class GraphChartBody {

    private Title title;
    private Tooltip tooltip;
    private List<GraphSeries> series;

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


}
