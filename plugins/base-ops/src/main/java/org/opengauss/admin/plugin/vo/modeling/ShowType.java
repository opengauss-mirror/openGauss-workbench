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
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class ShowType {

    private int key;
    private String dimension;
    private String field;
    private Boolean percentage;
    private String dateField;
    private Integer particle;
    private Integer compareWay;

    public void setKey(int key) {
        this.key = key;
    }
    public int getKey() {
        return key;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }
    public String getDimension() {
        return dimension;
    }

    public void setField(String field) {
        this.field = field;
    }
    public String getField() {
        return field;
    }

    public void setPercentage(Boolean percentage) {
        this.percentage = percentage;
    }
    public Boolean getPercentage() {
        return percentage;
    }

    public void setDateField(String dateField) {
        this.dateField = dateField;
    }
    public String getDateField() {
        return dateField;
    }

    public void setParticle(Integer particle) {
        this.particle = particle;
    }
    public Integer getParticle() {
        return particle;
    }

    public void setCompareWay(Integer compareWay) {
        this.compareWay = compareWay;
    }
    public Integer getCompareWay() {
        return compareWay;
    }

}
