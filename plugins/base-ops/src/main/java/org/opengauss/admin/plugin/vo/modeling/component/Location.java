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
public class Location {

    private String field;
    private int type;
    private Long commonValue;
    private String diyValue;
    private int area;
    public void setField(String field) {
        this.field = field;
    }
    public String getField() {
        return field;
    }

    public void setType(int type) {
        this.type = type;
    }
    public int getType() {
        return type;
    }

    public void setCommonValue(Long commonValue) {
        this.commonValue = commonValue;
    }
    public Long getCommonValue() {
        return commonValue;
    }

    public void setDiyValue(String diyValue) {
        this.diyValue = diyValue;
    }
    public String getDiyValue() {
        return diyValue;
    }

    public void setArea(int area) {
        this.area = area;
    }
    public int getArea() {
        return area;
    }

}
