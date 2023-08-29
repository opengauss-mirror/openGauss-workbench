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

import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class CategoryBody {

    private String field;
    private String categoryName;
    private Integer type;
    private List<String> valueList;

    public CategoryBody(String field, String categoryName, Integer type) {
        this.field = field;
        this.categoryName = categoryName;
        this.type = type;
    }

    public void setField(String field) {
        this.field = field;
    }
    public String getField() {
        return field;
    }

    public void setCategoryName(String field) {
        this.categoryName = field;
    }
    public String getCategoryName() {
        return categoryName;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public Integer getType() {
        return type == 0 ? 1 : type;
    }

    public void setValueList(List<String> valueList) {
        this.valueList = valueList;
    }
    public List<String> getValueList() {
        return this.valueList;
    }

}
