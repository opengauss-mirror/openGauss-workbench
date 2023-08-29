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
public class VisualMap {

    private int min;
    private int max;
    private List<String> text;
    private boolean calculable;
    private String orient;
    private String left;
    private String bottom;
    private InRange inRange;

    public VisualMap setMin(int min) {
        this.min = min;
        return this;
    }
    public int getMin() {
        return min;
    }

    public VisualMap setMax(int max) {
        this.max = max;
        return this;
    }
    public int getMax() {
        return max;
    }

    public VisualMap setCalculable(boolean calculable) {
        this.calculable = calculable;
        return this;
    }
    public boolean getCalculable() {
        return calculable;
    }

    public VisualMap setOrient(String orient) {
        this.orient = orient;
        return this;
    }
    public String getOrient() {
        return orient;
    }

    public VisualMap setLeft(String left) {
        this.left = left;
        return this;
    }
    public String getLeft() {
        return left;
    }

    public VisualMap setBottom(String bottom) {
        this.bottom = bottom;
        return this;
    }
    public String getBottom() {
        return bottom;
    }
    public VisualMap setInRange(InRange inRange) {
        this.inRange = inRange;
        return this;
    }
    public InRange getInRange() {
        return inRange;
    }
    public VisualMap setText(List<String> text) {
        this.text = text;
        return this;
    }
    public List<String> getText() {
        return text;
    }

}
