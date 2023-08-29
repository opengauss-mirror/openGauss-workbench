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
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class YAxis {

    private String type;
    private String name;
    private String position;
    private boolean alignTicks;
    private AxisLine axisLine;
    private AxisLabel axisLabel;
    private float min;

    public YAxis setType(String type) {
        this.type = type;
        return this;
    }
    public String getType() {
        return type;
    }

    public YAxis setName(String name) {
        this.name = name;
        return this;
    }
    public String getName() {
        return name;
    }

    public YAxis setPosition(String position) {
        this.position = position;
        return this;
    }
    public String getPosition() {
        return position;
    }

    public YAxis setAlignTicks(boolean alignTicks) {
        this.alignTicks = alignTicks;
        return this;
    }
    public boolean getAlignTicks() {
        return alignTicks;
    }

    public YAxis setAxisLine(AxisLine axisLine) {
        this.axisLine = axisLine;
        return this;
    }
    public AxisLine getAxisLine() {
        return axisLine;
    }

    public YAxis setAxisLabel(AxisLabel axisLabel) {
        this.axisLabel = axisLabel;
        return this;
    }
    public AxisLabel getAxisLabel() {
        return axisLabel;
    }

    public YAxis setMin(float min) {
        this.min = min;
        return this;
    }
    public float getMin() {
        return min;
    }

}
