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
public class Series {

    private String name;
    private String type;
    private String stack;
    private List<Float> data;
    private int yAxisIndex;
    private Label label;

    public Series setName(String name) {
        this.name = name;
        return this;
    }
    public String getName() {
        return name;
    }

    public Series setType(String type) {
        this.type = type;
        return this;
    }
    public String getType() {
        return type;
    }

    public Series setStack(String stack) {
        this.stack = stack;
        return this;
    }
    public String getStack() {
        return stack;
    }

    public Series setData(List<Float> data) {
        this.data = data;
        return this;
    }
    public List<Float> getData() {
        return data;
    }

    public Series setYAxisIndex(int yAxisIndex) {
        this.yAxisIndex = yAxisIndex;
        return this;
    }
    public int getYAxisIndex() {
        return yAxisIndex;
    }

    public Series setLabel(Label label) {
        this.label = label;
        return this;
    }
    public Label getLabel() {
        return label;
    }

}
