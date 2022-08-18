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
package org.opengauss.admin.plugin.vo.modeling.component;

import java.util.List;

/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class PieSeries {

    private String name;
    private String type;
    private List<PieData> data;
    private String selectedMode;
    private List<String> radius;
    private Label label;
    private LabelLine labelLine;

    public PieSeries setName(String name) {
        this.name = name;
        return this;
    }
    public String getName() {
        return name;
    }

    public PieSeries setType(String type) {
        this.type = type;
        return this;
    }
    public String getType() {
        return type;
    }

    public void setSelectedMode(String selectedMode) {
        this.selectedMode = selectedMode;
    }
    public String getSelectedMode() {
        return selectedMode;
    }

    public void setRadius(List<String> radius) {
        this.radius = radius;
    }
    public List<String> getRadius() {
        return radius;
    }

    public void setLabel(Label label) {
        this.label = label;
    }
    public Label getLabel() {
        return label;
    }

    public void setLabelLine(LabelLine labelLine) {
        this.labelLine = labelLine;
    }
    public LabelLine getLabelLine() {
        return labelLine;
    }

    public PieSeries setData(List<PieData> data) {
        this.data = data;
        return this;
    }

    public List<PieData> getData() {
        return data;
    }

}
