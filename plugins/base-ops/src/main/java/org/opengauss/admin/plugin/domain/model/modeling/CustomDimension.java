/*
 * Copyright (c) 2020 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
