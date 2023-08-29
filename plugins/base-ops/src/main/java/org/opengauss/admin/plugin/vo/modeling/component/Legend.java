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
public class Legend {

    private List<String> data;
    private String type;
    private String width;

    private int bottom;
    private int top;

    public Legend(String type) {
        this.type = type;
    }

    public Legend setData(List<String> data) {
        this.data = data;
        return this;
    }
    public List<String> getData() {
        return data;
    }

    public String getType() {
        return this.type;
    }

    public Legend setBottom(int bottom) {
        this.bottom = bottom;
        return this;
    }

    public int getBottom() {
        return this.bottom;
    }

    public Legend setTop(int top) {
        this.top = top;
        return this;
    }

    public int getTop() {
        return this.top;
    }

    public Legend setWidth(String width) {
        this.width = width;
        return this;
    }

    public String getWidth() {
        return this.width;
    }

}
