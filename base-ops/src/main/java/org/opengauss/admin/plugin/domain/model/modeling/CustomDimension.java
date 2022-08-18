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
package org.opengauss.admin.plugin.domain.model.modeling;

import org.opengauss.admin.plugin.vo.modeling.Categories;

import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 */
public class CustomDimension {

    private Long dataFlowId;
    private String operatorId;
    private String name;
    private String field;
    private List<Categories> categories;
    private Long id;
    public void setDataFlowId(Long dataFlowId) {
        this.dataFlowId = dataFlowId;
    }
    public Long getDataFlowId() {
        return dataFlowId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }
    public String getOperatorId() {
        return operatorId;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setField(String field) {
        this.field = field;
    }
    public String getField() {
        return field;
    }

    public void setCategories(List<Categories> categories) {
        this.categories = categories;
    }
    public List<Categories> getCategories() {
        return categories;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }


}
