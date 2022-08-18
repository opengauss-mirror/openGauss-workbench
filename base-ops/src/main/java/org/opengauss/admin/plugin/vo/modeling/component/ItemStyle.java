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
public class ItemStyle {

    private String color;
    private int shadowBlur;
    private String shadowColor;

    public ItemStyle setColor(String color) {
        this.color = color;
        return this;
    }
    public String getColor() {
        return color;
    }

    public ItemStyle setShadowBlur(int shadowBlur) {
        this.shadowBlur = shadowBlur;
        return this;
    }
    public int getShadowBlur() {
        return shadowBlur;
    }

    public ItemStyle setShadowColor(String shadowColor) {
        this.shadowColor = shadowColor;
        return this;
    }
    public String getShadowColor() {
        return shadowColor;
    }
}
